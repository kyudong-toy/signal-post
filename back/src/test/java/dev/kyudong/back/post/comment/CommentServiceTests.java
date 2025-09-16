package dev.kyudong.back.post.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.application.port.out.web.CommentPersistencePort;
import dev.kyudong.back.post.application.port.out.web.CommentQueryPort;
import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentSort;
import dev.kyudong.back.post.domain.entity.CommentStatus;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.exception.CommentNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.application.service.web.CommentService;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

public class CommentServiceTests extends UnitTestBase {

	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentQueryPort commentQueryPort;

	@Mock
	private PostUsecase postUsecase;

	@Mock
	private CommentPersistencePort commentPersistencePort;

	@Mock
	private UserReaderService userReaderService;

	@Mock
	private ObjectMapper objectMapper;

	@Nested
	@DisplayName("댓글 생성")
	class CreateComment {

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(eq(mockUser.getId()))).willReturn(mockUser);

			Post mockPost = createMockPost(mockUser);
			Long postId = mockPost.getId();
			given(postUsecase.getPostEntityOrThrow(postId)).willReturn(mockPost);

			Object content = createMockContentObject();
			CommentCreateReqDto request = new CommentCreateReqDto(content);
			Comment mockComment = makeMockComment(mockPost, mockUser);
			given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());
			given(commentPersistencePort.save(any(Comment.class))).willReturn(mockComment);

			// when
			CommentCreateResDto response = commentService.createComment(postId, mockUser.getId(), request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.postId()).isEqualTo(mockPost.getId());
			assertThat(response.userId()).isEqualTo(mockUser.getId());

			// 댓글 반영 확인
			assertThat(mockPost.getCommentList()).isNotEmpty();
			assertThat(mockPost.getCommentList()).hasSize(1);
			assertThat(mockPost.getCommentList().get(0).getContent()).isEqualTo(mockComment.getContent());
			then(commentPersistencePort).should().save(any(Comment.class));
		}

		@ParameterizedTest
		@DisplayName("실패 : 유효하지 않는 본문 요청")
		@ValueSource(strings = {
				"",
				"   "
		})
		void fail_invalidContent(String invalidContent) throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser);

			Post mockPost = createMockPost(mockUser);
			Long postId = mockPost.getId();
			given(postUsecase.getPostEntityOrThrow(eq(postId))).willReturn(mockPost);
			CommentCreateReqDto request = new CommentCreateReqDto(invalidContent);

			// when & then
			assertThatThrownBy(() -> commentService.createComment(mockPost.getId(), mockUser.getId(), request))
					.isInstanceOf(InvalidInputException.class);
			then(postUsecase).should().getPostEntityOrThrow(postId);
			then(commentPersistencePort).should(never()).save(any(Comment.class));
		}

		@Test
		@DisplayName("실패 : 존재하지 않은 게시물")
		void fail_postNotFound() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Long nonPostId = -1L;
			given(postUsecase.getPostEntityOrThrow(anyLong())).willThrow(new PostNotFoundException(nonPostId));

			Object content = createMockContent();
			CommentCreateReqDto request = new CommentCreateReqDto(content);

			// when & then
			assertThatThrownBy(() -> commentService.createComment(nonPostId, mockUser.getId(), request))
					.isInstanceOf(PostNotFoundException.class);
			then(postUsecase).should().getPostEntityOrThrow(anyLong());
			then(commentPersistencePort).should(never()).save(any(Comment.class));
		}

	}

	@Nested
	@DisplayName("댓글 수정")
	class UpdateComment {

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			Long postId = mockPost.getId();
			doNothing().when(postUsecase).validatePostExists(anyLong());

			Comment mockComment = makeMockComment(mockPost, mockUser);
			Long commentId = mockComment.getId();
			given(commentPersistencePort.findByIdOrThrow(eq(commentId))).willReturn(mockComment);
			given(objectMapper.writeValueAsString(any())).willReturn(createMockContent());

			CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

			// when
			CommentUpdateResDto response = commentService.updateComment(postId, commentId, mockUser.getId(), request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.postId()).isEqualTo(mockPost.getId());
			assertThat(response.userId()).isEqualTo(mockUser.getId());
			then(postUsecase).should().validatePostExists(anyLong());
			then(commentPersistencePort).should().findByIdOrThrow(anyLong());
		}

		@Test
		@DisplayName("실패 : 존재하지 않은 게시글")
		void fail_postNotFound() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			doThrow(new PostNotFoundException(mockPost.getId())).when(postUsecase).validatePostExists(anyLong());

			Long commentId = 999L;
			CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

			// when & then
			assertThatThrownBy(() -> commentService.updateComment(mockPost.getId(), commentId, mockUser.getId(), request))
					.isInstanceOf(PostNotFoundException.class);
			then(postUsecase).should().validatePostExists(anyLong());
			then(commentPersistencePort).should(never()).findByIdOrThrow(anyLong());
		}

		@Test
		@DisplayName("실패 : 존재하지 않는 댓글")
		void fail_commentNotFound() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			Long postId = mockPost.getId();
			doNothing().when(postUsecase).validatePostExists(anyLong());

			Long commentId = 999L;
			CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");
			given(commentPersistencePort.findByIdOrThrow(anyLong())).willThrow(new CommentNotFoundException(commentId));

			// when & then
			assertThatThrownBy(() -> commentService.updateComment(postId, commentId, mockUser.getId(), request))
					.isInstanceOf(CommentNotFoundException.class);
			then(postUsecase).should().validatePostExists(anyLong());
			then(commentPersistencePort).should().findByIdOrThrow(anyLong());
		}

	}

	@Nested
	@DisplayName("댓글 상태 수정")
	class UpdateCommentStatus {

		@Test
		@DisplayName("성공")
		void success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			doNothing().when(postUsecase).validatePostExists(anyLong());

			Comment mockComment = makeMockComment(mockPost, mockUser);
			Long commentId = mockComment.getId();
			given(commentPersistencePort.findByIdOrThrow(eq(commentId))).willReturn(mockComment);

			CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

			// when
			CommentStatusUpdateResDto response = commentService.updateCommentStatus(mockPost.getId(), commentId, mockUser.getId(), request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.postId()).isEqualTo(mockPost.getId());
			assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
			then(postUsecase).should().validatePostExists(anyLong());
			then(commentPersistencePort).should().findByIdOrThrow(anyLong());
		}

		@Test
		@DisplayName("실패 : 존재하지 않는 댓글")
		void fail_commentNotFound() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			doNothing().when(postUsecase).validatePostExists(anyLong());

			Long commentId = 999L;
			given(commentPersistencePort.findByIdOrThrow(anyLong()))
					.willThrow(new CommentNotFoundException(commentId));

			CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

			// when & then
			assertThatThrownBy(() -> commentService.updateCommentStatus(mockPost.getId(), commentId, mockUser.getId(), request))
					.isInstanceOf(CommentNotFoundException.class);
			then(postUsecase).should().validatePostExists(anyLong());
			then(commentPersistencePort).should().findByIdOrThrow(anyLong());
		}

		@Test
		@DisplayName("실패 : 댓글 수정자 권한없음")
		void fail_invalidAccess() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);
			doNothing().when(postUsecase).validatePostExists(anyLong());

			Comment mockComment = makeMockComment(mockPost, mockUser);
			Long commentId = mockComment.getId();
			given(commentPersistencePort.findByIdOrThrow(eq(commentId))).willReturn(mockComment);

			CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

			// when & then
			assertThatThrownBy(() -> commentService.updateCommentStatus(mockPost.getId(), commentId, 999L, request))
					.isInstanceOf(AccessDeniedException.class);
		}

	}

	@Nested
	@DisplayName("댓글 조회")
	class FindComments {

		@Test
		@DisplayName("성공 : 최신 댓글 목록 첫 조회")
		void findNewCommentsByPostId_success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			Post mockPost = createMockPost(mockUser);

			// 댓글 생성
			List<Comment> list = new ArrayList<>();
			for (int i = 1; i <= 100; i++) {
				Comment mockComment = makeMockComment(mockPost, mockUser);
				ReflectionTestUtils.setField(mockComment, "id", (long) i);
				mockPost.addComment(mockComment);
				list.add(mockComment);
			}

			given(commentQueryPort.findByNewCommentByCursor(mockPost.getId(), null)).willReturn(list);

			// when
			CommentListResDto response = commentService.findComments(mockPost.getId(), null, CommentSort.NEW);

			// then
			assertThat(response.comments().size()).isEqualTo(20);
			assertThat(response.hasNext()).isTrue();
			then(commentQueryPort).should().findByNewCommentByCursor(anyLong(), isNull());
			then(commentQueryPort).should(never()).findByOldCommentByCursor(anyLong(), anyLong());
		}

	}

}
