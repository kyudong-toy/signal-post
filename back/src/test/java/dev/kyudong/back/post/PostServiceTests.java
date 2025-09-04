package dev.kyudong.back.post;

import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.vo.EditorBlockVO;
import dev.kyudong.back.post.api.dto.req.vo.EditorContentVO;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.domain.PostStatus;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.post.service.PostService;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserRepository userRepository;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private PostService postService;

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	private static Post makeMockPost(User mockUser) {
		Post mockPost = Post.builder()
				.subject("subject")
				.content(createTestContent())
				.build();
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	private static String createTestContent() {
		return "{\"time\": 1756713679939, \"blocks\": [{\"id\": \"test-block\", \"data\": {\"text\": \"테스트!\"}, \"type\": \"paragraph\"}], \"version\": \"2.31.0\"}";
	}

	private static Post makeMockPost(Long postId, User mockUser) {
		Post mockPost = Post.builder()
				.subject("subject")
				.content("content")
				.build();
		ReflectionTestUtils.setField(mockPost, "id", postId);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	@Test
	@DisplayName("게시글 생성 - 성공")
	void createPost_success() {
		// given
		User mockUser = makeMockUser();
		when(userRepository.existsById(eq(mockUser.getId()))).thenReturn(true);
		when(userRepository.getReferenceById(eq(mockUser.getId()))).thenReturn(mockUser);

		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", "테스트")), "1.0");
		PostCreateReqDto request = new PostCreateReqDto("subject", content, new HashSet<>());
		Post mockPost = makeMockPost(mockUser);
		when(postRepository.save(any(Post.class))).thenReturn(mockPost);

		// when
		PostCreateResDto response = postService.createPost(mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.subject()).isEqualTo("subject");
		assertThat(response.content()).isEqualTo("content");

		// 게시글 반영 확인
		assertThat(mockUser.getPostList()).isNotEmpty();
		assertThat(mockUser.getPostList()).hasSize(1);
		verify(postRepository, times(1)).save(any(Post.class));
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
	void createPost_fail_invalidSubject(String invalidSubject) {
		// given
		User mockUser = makeMockUser();
		when(userRepository.existsById(eq(mockUser.getId()))).thenReturn(true);
		when(userRepository.getReferenceById(eq(mockUser.getId()))).thenReturn(mockUser);

		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", "테스트")), "1.0");
		PostCreateReqDto request = new PostCreateReqDto(invalidSubject, content, new HashSet<>());

		// when & then
		assertThatThrownBy(() -> postService.createPost(mockUser.getId(), request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("Subject");
		verify(postRepository, never()).save(any(Post.class));
	}

	// 게시글 본문 요청에 사용
	private static Stream<Arguments> provideInvalidContent() {
		return Stream.of(
				Arguments.of((String) null),       		// 1. null
				Arguments.of("")          // 2. 빈 문자열 ""
		);
	}

	@ParameterizedTest
	@DisplayName("게시글 생성 - 실패 : 유효하지 않는 본문")
	@MethodSource("provideInvalidContent")
	void createPost_fail_invalidContent(String invalidContent) {
		// given
		User mockUser = makeMockUser();
		when(userRepository.getReferenceById(eq(mockUser.getId()))).thenReturn(mockUser);
		when(userRepository.existsById(eq(mockUser.getId()))).thenReturn(true);

		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", invalidContent)), "1.0");
		PostCreateReqDto request = new PostCreateReqDto("subject", content, new HashSet<>());

		// when & then
		assertThatThrownBy(() -> postService.createPost(mockUser.getId(), request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("Content");
		verify(postRepository, never()).save(any(Post.class));
	}

	@Test
	@DisplayName("게시글 수정 - 성공")
	void updatePost_success() {
		// given
		User mockUser = makeMockUser();
		final Long postId = 1L;
		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", "테스트")), "1.0");
		PostUpdateReqDto request = new PostUpdateReqDto("newSubject", content, new HashSet<>(), new HashSet<>());
		Post mockPost = makeMockPost(postId, mockUser);
		when(postRepository.findById(eq(postId))).thenReturn(Optional.of(mockPost));

		// when
		PostUpdateResDto response = postService.updatePost(postId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.subject()).isEqualTo("newSubject");
		assertThat(response.content()).isEqualTo("newContent");
	}

	@Test
	@DisplayName("게시글 수정 - 실패 : 존재하지 않는 게시글")
	void updatePost_fail_postNotFound() {
		// given
		User mockUser = makeMockUser();
		final Long postId = 1L;
		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", "test")), "1.0");
		PostUpdateReqDto request = new PostUpdateReqDto("newSubject", content, new HashSet<>(), new HashSet<>());
		when(postRepository.findById(eq(postId))).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.updatePost(postId, mockUser.getId(), request))
				.isInstanceOf(PostNotFoundException.class)
				.hasMessage("Post {" + postId + "} Not Found");
		verify(postRepository, times(1)).findById(postId);
	}

	@Test
	@DisplayName("게시글 수정 - 실패 : 게시글 수정자 권한 없음")
	void updatePost_fail_invalidAccess() {
		// given
		User mockUser = makeMockUser();
		final Long postId = 1L;
		EditorContentVO content = new EditorContentVO(1L, List.of(new EditorBlockVO("1", "paragraph", "test")), "1.0");
		PostUpdateReqDto request = new PostUpdateReqDto("newSubject", content, new HashSet<>(), new HashSet<>());
		Post mockPost = makeMockPost(postId, mockUser);
		when(postRepository.findById(eq(postId))).thenReturn(Optional.of(mockPost));

		// when & then
		assertThatThrownBy(() -> postService.updatePost(postId, 999L, request))
				.isInstanceOf(InvalidAccessException.class)
				.hasMessage("User {" + 999L + "} has no permission to update post " + postId);
	}

	@Test
	@DisplayName("게시글 상태 변경 - 성공")
	void updatePostStatus_success() {
		// given
		User mockUser = makeMockUser();
		long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);
		Post mockPost = makeMockPost(postId, mockUser);
		when(postRepository.findById(eq(postId))).thenReturn(Optional.of(mockPost));

		// when
		PostStatusUpdateResDto response = postService.updatePostStatus(postId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.status()).isEqualTo(PostStatus.DELETED);
	}

	@Test
	@DisplayName("게시글 상태 변경 - 실패 : 게시글 수정자 권한 없음")
	void updatePostStatus_fail_invalidAccess() {
		// given
		User mockUser = makeMockUser();
		final Long postId = 1L;
		final Long nonExistsUserId = 999L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.NORMAL);
		Post mockPost = makeMockPost(postId, mockUser);
		mockPost.delete();
		when(postRepository.findById(eq(postId))).thenReturn(Optional.of(mockPost));

		// when && then
		assertThatThrownBy(() -> postService.updatePostStatus(postId, nonExistsUserId, request))
				.isInstanceOf(InvalidAccessException.class)
				.hasMessage("User {" + nonExistsUserId + "} has no permission to update post status " + postId);
	}

	@Test
	@DisplayName("게시글 조회 - 성공")
	void findPost_success() {
		// given
		User mockUser = makeMockUser();
		long postId = 1L;
		Post mockPost = makeMockPost(postId, mockUser);
		when(postRepository.findById(eq(postId))).thenReturn(Optional.of(mockPost));

		// when
		PostDetailResDto response = postService.findPostById(postId);

		// then
		verify(postRepository, times(1)).findById(postId);
		assertThat(response.postId()).isEqualTo(postId);
		assertThat(response.subject()).isNotNull();
		assertThat(response.content()).isNotNull();
		assertThat(response.createdAt()).isNotNull();
		assertThat(response.modifiedAt()).isNotNull();
		assertThat(response.status()).isNotNull();
	}

	@Test
	@DisplayName("게시글 조회 - 실패 : 존재하지 않는 게시글")
	void findPost_fail_postNotFound() {
		// given
		long postId = 1L;
		when(postRepository.findById(eq(postId))).thenReturn(Optional.empty());

		// when && then
		assertThatThrownBy(() -> postService.findPostById(postId))
				.isInstanceOf(PostNotFoundException.class)
				.hasMessage("Post {" + postId + "} Not Found");
	}

}
