package dev.kyudong.back.post.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.common.exception.InvalidInputException;
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
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

public class PostServiceTests extends UnitTestBase {

	@Mock
	private UserReaderService userReaderService;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private PostService postService;

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

	@Nested
	@DisplayName("게시글 생성")
	class CreatePost {

		@Test
		@DisplayName("성공 : 태그 없음")
		void success() throws Exception {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(eq(mockUser.getId()))).willReturn(mockUser);

			Object content = createMockContentObject();
			PostCreateReqDto request = new PostCreateReqDto(
					"subject",
					content,
					new HashSet<>(),
					new HashSet<>()
			);
			Post mockPost = createMockPost(mockUser);
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
		@DisplayName("성공 : 태그 있음")
		void success_withTags() throws Exception {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(eq(mockUser.getId()))).willReturn(mockUser);

			Object content = createMockContentObject();
			PostCreateReqDto request = new PostCreateReqDto(
					"subject",
					content,
					new HashSet<>(),
					Set.of("새로운태그", "기존태그")
			);
			Post mockPost = createMockPost(mockUser);
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

		@ParameterizedTest
		@DisplayName("실패 : 유효하지 않는 제목")
		@ValueSource(strings = {
				"",
				"   ",
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
		})
		void createPost_fail_invalidSubject(String invalidSubject) throws JsonProcessingException {
			// given
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

	}

	@Nested
	@DisplayName("게시글 수정 - 성공")
	class UpdatePost {

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			PostUpdateReqDto request = new PostUpdateReqDto(
					"newSubject",
					createMockContentObject(),
					new HashSet<>(),
					new HashSet<>()
			);

			Post mockPost = createMockPost(mockUser);
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
		@DisplayName("실패 : 존재하지 않는 게시글")
		void fail_postNotFound() {
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
		@DisplayName("실패 : 게시글 수정자 권한 없음")
		void fail_invalidAccess() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			PostUpdateReqDto request = new PostUpdateReqDto(
					"newSubject",
					createMockContentObject(),
					new HashSet<>(),
					new HashSet<>()
			);

			Post mockPost = createMockPost(mockUser);
			given(postPersistencePort.findByIdOrThrow(anyLong())).willReturn(mockPost);

			// when & then
			assertThatThrownBy(() -> postService.updatePost(postId, 999L, request))
					.isInstanceOf(AccessDeniedException.class);
		}

	}

	@Nested
	@DisplayName("게시글 상태 변경")
	class UpdatePostStatus {

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);

			Post mockPost = createMockPost(mockUser);
			given(postPersistencePort.findByIdOrThrow(postId)).willReturn(mockPost);

			// when
			PostStatusUpdateResDto response = postService.updatePostStatus(postId, mockUser.getId(), request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.status()).isEqualTo(PostStatus.DELETED);
		}

		@Test
		@DisplayName("실패 : 게시글 수정자 권한 없음")
		void fail_invalidAccess() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			mockPost.delete();

			final Long postId = 1L;
			given(postPersistencePort.findByIdOrThrow(postId)).willReturn(mockPost);

			final Long nonExistsUserId = 999L;
			PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.NORMAL);

			// when && then
			assertThatThrownBy(() -> postService.updatePostStatus(postId, nonExistsUserId, request))
					.isInstanceOf(AccessDeniedException.class);
		}

	}

	@Nested
	@DisplayName("게시글 조회")
	class FindPost {

		@Test
		@DisplayName("성공 : 사용자")
		void success_user() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			Post mockPost = createMockPost(mockUser);

			given(postPersistencePort.findByIdOrThrow(eq(postId))).willReturn(mockPost);

			doNothing().when(postViewEventPublishPort).increasePostViewWithUser(any(User.class), any(Post.class));

			doReturn(bloomFilter).when(redissonClient).getBloomFilter(anyString());
			given(bloomFilter.add(anyLong())).willReturn(true);

			given(userReaderService.getUserReference(999L)).willReturn(createMockUser());

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
		@DisplayName("성공 : 게스트")
		void success_guest() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			Post mockPost = createMockPost(mockUser);

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
		@DisplayName("실패 : 사용자로 조회 결과, 존재하지 않는 게시글")
		void fail_user_postNotFound() {
			// given
			User mockUser = createMockUser();
			final Long postId = 1L;
			given(postPersistencePort.findByIdOrThrow(eq(postId))).willThrow(new PostNotFoundException(postId));

			// when && then
			assertThatThrownBy(() -> postService.findPostByIdWithUser(mockUser.getId(), postId))
					.isInstanceOf(PostNotFoundException.class);
		}

		@Test
		@DisplayName("실패 : 게스트로 조회 결과, 존재하지 않는 게시글")
		void fail_guest_postNotFound() {
			// given
			final Long postId = 1L;

			String guestId = UUID.randomUUID().toString();

			given(postPersistencePort.findByIdOrThrow(eq(postId))).willThrow(new PostNotFoundException(postId));

			// when && then
			assertThatThrownBy(() -> postService.findPostByIdWithGuest(guestId, postId))
					.isInstanceOf(PostNotFoundException.class);
		}

	}

}
