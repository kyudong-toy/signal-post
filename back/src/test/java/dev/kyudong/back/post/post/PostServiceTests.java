package dev.kyudong.back.post.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.application.port.in.web.CategoryUsecase;
import dev.kyudong.back.post.application.port.in.web.TagUsecase;
import dev.kyudong.back.post.application.port.out.event.PostEventPublishPort;
import dev.kyudong.back.post.application.port.out.event.PostViewEventPublishPort;
import dev.kyudong.back.post.application.port.out.web.PostPersistencePort;
import dev.kyudong.back.post.application.service.web.PostService;
import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {

	@Mock
	private UserService userService;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private PostService postService;

	@Mock
	private CategoryUsecase categoryUsecase;

	@Mock
	private TagUsecase tagUsecase;

	@Mock
	private PostPersistencePort postPersistencePort;

	@Mock
	private PostEventPublishPort postEventPublishPort;

	@Mock
	private PostViewEventPublishPort postViewEventPublishPort;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RBloomFilter<Long> bloomFilter;

	private static User createMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	private static Post createMockPost(User mockUser, Category category) throws JsonProcessingException {
		Post mockPost = Post.create("제목", createMockContent());
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		ReflectionTestUtils.setField(mockPost, "postViewCount", 0L);
		ReflectionTestUtils.setField(mockPost, "createdAt", Instant.now());
		ReflectionTestUtils.setField(mockPost, "modifiedAt", Instant.now());
		return mockPost;
	}

	private static Category createMockCategory() {
		Category mockCategory = Category.builder()
				.categoryCode("test_1")
				.build();
		ReflectionTestUtils.setField(mockCategory, "id", 1L);
		return mockCategory;
	}

	private static String createMockContent() throws JsonProcessingException {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"contents", List.of(textNode)
		);

		Map<String, Object> map = Map.of(
				"type", "doc",
				"contents", List.of(paragraphNode)
		);

		return new ObjectMapper().writeValueAsString(map);
	}

	private static Map<String, Object> createMockContentObject() {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"contents", List.of(textNode)
		);

		return Map.of(
				"type", "doc",
				"contents", List.of(paragraphNode)
		);
	}

	@Test
	@DisplayName("게시글 생성 - 성공 : 태그 없음")
	void createPost_success() throws Exception {
		// given
		User mockUser = createMockUser();
		given(userService.getUserProxy(eq(mockUser.getId()))).willReturn(mockUser);

		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		given(categoryUsecase.findByCategoryCode(anyString())).willReturn(mockCategory);

		Object content = createMockContentObject();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				content,
				new HashSet<>(),
				new HashSet<>()
		);
		Post mockPost = createMockPost(mockUser, mockCategory);
		given(postPersistencePort.save(any(Post.class))).willReturn(mockPost);
		given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());
		given(tagUsecase.caretedNewTag(anySet())).willReturn(new HashSet<>());
		doNothing().when(postEventPublishPort).postCreateEventPublish(any(Post.class), anySet());

		// when
		PostCreateResDto response = postService.createPost(mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.subject()).isEqualTo("제목");

		// 게시글 반영 확인
		assertThat(mockUser.getPostList()).isNotEmpty();
		assertThat(mockUser.getPostList()).hasSize(1);
	}

	@Test
	@DisplayName("게시글 생성 - 성공 : 태그 있음")
	void createPost_success_withTags() throws Exception {
		// given
		User mockUser = createMockUser();
		given(userService.getUserProxy(eq(mockUser.getId()))).willReturn(mockUser);

		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		given(categoryUsecase.findByCategoryCode(anyString())).willReturn(mockCategory);

		Object content = createMockContentObject();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				content,
				new HashSet<>(),
				Set.of("새로운태그", "기존태그")
		);
		Post mockPost = createMockPost(mockUser, mockCategory);
		given(postPersistencePort.save(any(Post.class))).willReturn(mockPost);
		given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());
		given(tagUsecase.caretedNewTag(anySet())).willReturn(new HashSet<>());
		doNothing().when(postEventPublishPort).postCreateEventPublish(any(Post.class), anySet());

		// when
		PostCreateResDto response = postService.createPost(mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.subject()).isEqualTo("제목");

		// 게시글 반영 확인
		assertThat(mockUser.getPostList()).isNotEmpty();
		assertThat(mockUser.getPostList()).hasSize(1);
	}

	// 게시글 제목 요청에 사용
	private static Stream<Arguments> provideInvalidSubject() {
		return Stream.of(
				Arguments.of((String) null),       		// 1. null
				Arguments.of(""),          // 2. 빈 문자열 ""
				Arguments.of(" "),         // 3. 공백 문자 " "
				Arguments.of("a".repeat(101))    // 4. 101자 문자열
		);
	}

	@ParameterizedTest
	@DisplayName("게시글 생성 - 실패 : 유효하지 않는 제목")
	@MethodSource("provideInvalidSubject")
	void createPost_fail_invalidSubject(String invalidSubject) throws JsonProcessingException {
		// given
		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		given(categoryUsecase.findByCategoryCode(anyString())).willReturn(mockCategory);

		User mockUser = createMockUser();
		given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());

		PostCreateReqDto request = new PostCreateReqDto(
				invalidSubject,
				createMockContentObject(),
				new HashSet<>(),
				new HashSet<>()
		);

		// when & then
		assertThatThrownBy(() -> postService.createPost(mockUser.getId(), request))
				.isInstanceOf(InvalidInputException.class);
	}

	@Test
	@DisplayName("게시글 수정 - 성공")
	void updatePost_success() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(
				"newSubject",
				createMockContentObject(),
				new HashSet<>(),
				new HashSet<>()
		);

		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);
		given(postPersistencePort.findByIdOrThrow(postId)).willReturn(mockPost);
		given(tagUsecase.caretedNewTag(anySet())).willReturn(new HashSet<>());
		given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());
		doNothing().when(postEventPublishPort).postUpdateEventPublish(any(Post.class), anySet());

		// when
		PostUpdateResDto response = postService.updatePost(postId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.subject()).isEqualTo("newSubject");
		assertThat(response.content()).isEqualTo(objectMapper.writeValueAsString(createMockContentObject()));
	}

	@Test
	@DisplayName("게시글 수정 - 실패 : 존재하지 않는 게시글")
	void updatePost_fail_postNotFound() {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(
				"newSubject",
				createMockContentObject(),
				new HashSet<>(),
				new HashSet<>()
		);

		given(postPersistencePort.findByIdOrThrow(postId)).willThrow(new PostNotFoundException(postId));

		// when & then
		assertThatThrownBy(() -> postService.updatePost(postId, mockUser.getId(), request))
				.isInstanceOf(PostNotFoundException.class);
	}

	@Test
	@DisplayName("게시글 수정 - 실패 : 게시글 수정자 권한 없음")
	void updatePost_fail_invalidAccess() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(
				"newSubject",
				createMockContentObject(),
				new HashSet<>(),
				new HashSet<>()
		);

		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);
		given(postPersistencePort.findByIdOrThrow(anyLong())).willReturn(mockPost);

		// when & then
		assertThatThrownBy(() -> postService.updatePost(postId, 999L, request))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("게시글 상태 변경 - 성공")
	void updatePostStatus_success() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);

		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);
		given(postPersistencePort.findByIdOrThrow(postId)).willReturn(mockPost);

		// when
		PostStatusUpdateResDto response = postService.updatePostStatus(postId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.status()).isEqualTo(PostStatus.DELETED);
	}

	@Test
	@DisplayName("게시글 상태 변경 - 실패 : 게시글 수정자 권한 없음")
	void updatePostStatus_fail_invalidAccess() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);
		mockPost.delete();

		final Long postId = 1L;
		given(postPersistencePort.findByIdOrThrow(postId)).willReturn(mockPost);

		final Long nonExistsUserId = 999L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.NORMAL);

		// when && then
		assertThatThrownBy(() -> postService.updatePostStatus(postId, nonExistsUserId, request))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@DisplayName("사용자 게시글 조회 - 성공")
	void findPost_withUser_success() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);

		given(postPersistencePort.findByIdOrThrow(eq(postId))).willReturn(mockPost);

		doNothing().when(postViewEventPublishPort).increasePostViewWithUser(any(User.class), any(Post.class));

		doReturn(bloomFilter).when(redissonClient).getBloomFilter(anyString());
		given(bloomFilter.add(anyLong())).willReturn(true);

		given(userService.getUserProxy(999L)).willReturn(createMockUser());

		// when
		PostDetailResDto response = postService.findPostByIdWithUser(999L, mockPost.getId());

		// then
		then(postPersistencePort).should(times(1)).findByIdOrThrow(postId);
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.subject()).isNotNull();
		assertThat(response.content()).isNotNull();
		assertThat(response.createdAt()).isNotNull();
		assertThat(response.modifiedAt()).isNotNull();
		assertThat(response.status()).isNotNull();
	}

	@Test
	@DisplayName("게스트 게시글 조회 - 성공")
	void findPost_withGuest_success() throws JsonProcessingException {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		Category mockCategory = createMockCategory();
		mockCategory.addTranslation("ko-KR", "일상(ko)");
		Post mockPost = createMockPost(mockUser, mockCategory);

		given(postPersistencePort.findByIdOrThrow(eq(postId))).willReturn(mockPost);

		String guestId = UUID.randomUUID().toString();
		doNothing().when(postViewEventPublishPort).increasePostViewWithGuest(anyString(), any(Post.class));

		doReturn(bloomFilter).when(redissonClient).getBloomFilter(anyString());
		given(bloomFilter.add(anyLong())).willReturn(true);

		// when
		PostDetailResDto response = postService.findPostByIdWithGuest(guestId, postId);

		// then
		then(postPersistencePort).should(times(1)).findByIdOrThrow(postId);
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.subject()).isNotNull();
		assertThat(response.content()).isNotNull();
		assertThat(response.createdAt()).isNotNull();
		assertThat(response.modifiedAt()).isNotNull();
		assertThat(response.status()).isNotNull();
	}

	@Test
	@DisplayName("게시글 조회 - 실패 : 존재하지 않는 게시글")
	void findPost_withUser_fail_postNotFound() {
		// given
		User mockUser = createMockUser();
		final Long postId = 1L;
		given(postPersistencePort.findByIdOrThrow(eq(postId))).willThrow(new PostNotFoundException(postId));

		// when && then
		assertThatThrownBy(() -> postService.findPostByIdWithUser(mockUser.getId(), postId))
				.isInstanceOf(PostNotFoundException.class);
	}

	@Test
	@DisplayName("게시글 조회 - 실패 : 존재하지 않는 게시글")
	void findPost_withGuest_fail_postNotFound() {
		// given
		final Long postId = 1L;

		String guestId = UUID.randomUUID().toString();

		given(postPersistencePort.findByIdOrThrow(eq(postId))).willThrow(new PostNotFoundException(postId));

		// when && then
		assertThatThrownBy(() -> postService.findPostByIdWithGuest(guestId, postId))
				.isInstanceOf(PostNotFoundException.class);
	}

}
