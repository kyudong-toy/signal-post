package dev.kyudong.back.post.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentUpdateResDto;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.exception.CommentNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.repository.CommentRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.post.application.service.web.CommentService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {

	@Mock
	private PostRepository postRepository;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CommentService commentService;

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	private static String createMockTiptapContent() throws JsonProcessingException {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"content", List.of(textNode)
		);

		Map<String, Object> map = Map.of(
				"type", "doc",
				"content", List.of(paragraphNode)
		);

		return new ObjectMapper().writeValueAsString(map);
	}

	private static Post makeMockPost(User mockUser) throws JsonProcessingException {
		Post mockPost = Post.create("제목", createMockTiptapContent(), Category.builder().build());
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	private static Comment makeMockComment(Post mockPost, User mockUser) {
		Comment mockComment = Comment.builder()
				.content("Hello Java!")
				.user(mockUser)
				.build();
		ReflectionTestUtils.setField(mockComment, "id", 1L);
		ReflectionTestUtils.setField(mockComment, "post", mockPost);
		ReflectionTestUtils.setField(mockComment, "createdAt", Instant.now());
		ReflectionTestUtils.setField(mockComment, "modifiedAt", Instant.now());
		return mockComment;
	}

	@Test
	@DisplayName("댓글 생성 - 성공")
	void createComment_success() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		when(userRepository.existsById(eq(mockUser.getId()))).thenReturn(true);
		when(userRepository.getReferenceById(eq(mockUser.getId()))).thenReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		when(postRepository.existsById(eq(mockPost.getId()))).thenReturn(true);
		when(postRepository.getReferenceById(eq(postId))).thenReturn(mockPost);

		CommentCreateReqDto request = new CommentCreateReqDto("Hello Comment");
		Comment mockComment = makeMockComment(mockPost, mockUser);
		ReflectionTestUtils.setField(mockComment, "contents", "Hello Comment");
		when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

		// when
		CommentCreateResDto response = commentService.createComment(postId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.userId()).isEqualTo(mockUser.getId());
		assertThat(response.content()).isEqualTo(request.content());

		// 댓글 반영 확인
		assertThat(mockPost.getCommentList()).isNotEmpty();
		assertThat(mockPost.getCommentList()).hasSize(1);
		assertThat(mockPost.getCommentList().get(0).getContent()).isEqualTo(mockComment.getContent());
		verify(commentRepository, times(1)).save(any(Comment.class));
	}

	// 댓글 본문 요청에 사용
	private static Stream<Arguments> provideInvalidContent() {
		return Stream.of(
				Arguments.of((String) null),       		// 1. null
				Arguments.of(""),          // 2. 빈 문자열 ""
				Arguments.of(" ")          // 3. 공백 문자 " "
		);
	}

	@ParameterizedTest
	@DisplayName("댓글 생성 - 실패 : 유효하지 않는 본문 요청")
	@MethodSource("provideInvalidContent")
	void createComment_fail_invalidContent(String invalidContent) throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		when(userRepository.existsById(eq(mockUser.getId()))).thenReturn(true);
		when(userRepository.getReferenceById(eq(mockUser.getId()))).thenReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		when(postRepository.existsById(eq(mockPost.getId()))).thenReturn(true);
		when(postRepository.getReferenceById(eq(postId))).thenReturn(mockPost);
		CommentCreateReqDto request = new CommentCreateReqDto(invalidContent);

		// when & then
		assertThatThrownBy(() -> commentService.createComment(mockPost.getId(), mockUser.getId(), request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("Content");
		verify(userRepository, times(1)).existsById(eq(mockUser.getId()));
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 생성 - 실패 : 존재하지 않은 게시물")
	void createComment_fail_postNotFound() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		when(postRepository.existsById(eq(mockPost.getId()))).thenReturn(false);
		CommentCreateReqDto request = new CommentCreateReqDto("Hello");

		// when & then
		assertThatThrownBy(() -> commentService.createComment(mockPost.getId(), mockUser.getId(), request))
				.isInstanceOf(PostNotFoundException.class)
				.hasMessage("Post {" + mockPost.getId() + "} Not Found");
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 수정 - 성공")
	void updateComment_success() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		when(postRepository.existsById(eq(postId))).thenReturn(true);

		Comment mockComment = makeMockComment(mockPost, mockUser);
		Long commentId = mockComment.getId();
		when(commentRepository.findById(eq(commentId))).thenReturn(Optional.of(mockComment));

		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

		// when
		CommentUpdateResDto response = commentService.updateComment(postId, commentId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.userId()).isEqualTo(mockUser.getId());
		assertThat(response.content()).isEqualTo(request.content());
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, times(1)).findById(eq(mockComment.getId()));
	}

	@Test
	@DisplayName("댓글 수정 - 실패 : 존재하지 않은 게시글")
	void updateComment_fail_postNotFound() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		when(postRepository.existsById(eq(postId))).thenReturn(false);
		Long commentId = 999L;
		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

		// when & then
		assertThatThrownBy(() -> commentService.updateComment(mockPost.getId(), commentId, mockUser.getId(), request))
				.isInstanceOf(PostNotFoundException.class)
				.hasMessage("Post {" + mockPost.getId() + "} Not Found");
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, never()).findById(eq(999L));
	}

	@Test
	@DisplayName("댓글 수정 - 실패 : 존재하지 않는 댓글")
	void updateComment_fail_commentNotFound() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		when(postRepository.existsById(eq(postId))).thenReturn(true);

		Long commentId = 999L;
		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.updateComment(postId, commentId, mockUser.getId(), request))
				.isInstanceOf(CommentNotFoundException.class)
				.hasMessage("Comment {" + commentId + "} Not Found");
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, times(1)).findById(eq(999L));
	}

	@Test
	@DisplayName("댓글 상태 수정 - 성공")
	void updateCommentStatus_success() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		when(postRepository.existsById(mockPost.getId())).thenReturn(true);

		Comment mockComment = makeMockComment(mockPost, mockUser);
		Long commentId = mockComment.getId();
		when(commentRepository.findById(eq(commentId))).thenReturn(Optional.of(mockComment));

		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

		// when
		CommentStatusUpdateResDto response = commentService.updateCommentStatus(mockPost.getId(), commentId, mockUser.getId(), request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(mockPost.getId());
		assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, times(1)).findById(eq(commentId));
	}

	@Test
	@DisplayName("댓글 상태 수정 - 실패 : 존재하지 않는 댓글")
	void updateCommentStatus_fail_commentNotFound() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		when(postRepository.existsById(mockPost.getId())).thenReturn(true);

		Comment mockComment = makeMockComment(mockPost, mockUser);
		Long commentId = mockComment.getId();
		when(commentRepository.findById(eq(commentId))).thenReturn(Optional.empty());

		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

		// when & then
		assertThatThrownBy(() -> commentService.updateCommentStatus(mockPost.getId(), commentId, mockUser.getId(), request))
				.isInstanceOf(CommentNotFoundException.class)
				.hasMessage("Comment {" + commentId + "} Not Found");
		verify(postRepository, times(1)).existsById(eq(mockPost.getId()));
		verify(commentRepository, times(1)).findById(eq(mockComment.getId()));
	}

	@Test
	@DisplayName("댓글 상태 수정 - 실패 : 댓글 수정자 권한없음")
	void updateCommentStatus_fail_invalidAccess() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		when(postRepository.existsById(mockPost.getId())).thenReturn(true);

		Comment mockComment = makeMockComment(mockPost, mockUser);
		Long commentId = mockComment.getId();
		when(commentRepository.findById(eq(commentId))).thenReturn(Optional.of(mockComment));

		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

		// when & then
		assertThatThrownBy(() -> commentService.updateCommentStatus(mockPost.getId(), commentId, 999L, request))
				.isInstanceOf(InvalidAccessException.class)
				.hasMessage("User {" + 999L + "} has no permission to update Comment status " + commentId);
	}

	@Test
	@DisplayName("댓글 목록 조회 - 성공")
	void findCommentsByPostId_success() throws JsonProcessingException {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);

		// 댓글 생성
		Comment mockComment1 = makeMockComment(mockPost, mockUser);
		ReflectionTestUtils.setField(mockComment1, "id", 1L);
		mockPost.addComment(mockComment1);
		Comment mockComment2 = makeMockComment(mockPost, mockUser);
		ReflectionTestUtils.setField(mockComment2, "id", 2L);
		mockPost.addComment(mockComment2);
		Comment mockComment3 = makeMockComment(mockPost, mockUser);
		ReflectionTestUtils.setField(mockComment3, "id", 3L);
		mockPost.addComment(mockComment3);

		List<Comment> commentList = List.of(mockComment1, mockComment2, mockComment3);
		when(commentRepository.findByPostId(eq(mockPost.getId()))).thenReturn(commentList);

		// when
		List<CommentDetailResDto> response = commentService.findCommentsByPostId(mockPost.getId());

		// then
		assertThat(response.size()).isEqualTo(commentList.size());
		assertThat(response.get(0).commentId()).isEqualTo(commentList.get(0).getId());
		verify(commentRepository, times(1)).findByPostId(eq(mockPost.getId()));
	}

}
