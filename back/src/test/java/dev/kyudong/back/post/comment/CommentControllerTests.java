package dev.kyudong.back.post.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.adapter.in.web.CommentController;
import dev.kyudong.back.post.application.port.in.web.CommentUsecase;
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
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.*;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
public class CommentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private CommentUsecase commentUsecase;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("rawPassword")
				.encodedPassword("encodedPassword")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	private static Post makeMockPost(User mockUser) {
		Post mockPost = Post.create("제목", "테스트본문");
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	private static Comment makeMockComment(Post mockPost, User mockUser) {
		Comment mockComment = Comment.create("테스트", mockUser);
		ReflectionTestUtils.setField(mockComment, "id", 1L);
		ReflectionTestUtils.setField(mockComment, "post", mockPost);
		ReflectionTestUtils.setField(mockComment, "createdAt", Instant.now());
		ReflectionTestUtils.setField(mockComment, "modifiedAt", Instant.now());
		return mockComment;
	}

	@Test
	@DisplayName("댓글 목록 조회 API - 성공")
	void findCommentsByPostIdApi_success() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		List<Comment> comments = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			Comment mockComment = makeMockComment(mockPost, mockUser);
			ReflectionTestUtils.setField(mockComment, "id", (long) i);
			mockPost.addComment(mockComment);
			comments.add(mockComment);
		}

		CommentListResDto response = CommentListResDto.from(comments);
		given(commentUsecase.findComments(anyLong(), isNull(), any(CommentSort.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId)
						.param("sort", "NEW"))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 작성 API - 성공")
	@WithMockCustomUser
	void createCommentApi_success() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		CommentCreateReqDto request = new CommentCreateReqDto("Hello Comment");
		CommentCreateResDto response = new CommentCreateResDto(
				mockUser.getId(), mockPost.getId(), 1L, "Hello Comment", CommentStatus.NORMAL,
				LocalDateTime.now(), LocalDateTime.now()
		);
		given(commentUsecase.createComment(eq(postId), eq(mockUser.getId()), any(CommentCreateReqDto.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.userId").value(mockUser.getId()))
				.andExpect(jsonPath("$.content").value("Hello Comment"))
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 작성 API - 실패")
	@WithMockCustomUser
	void createCommentApi_fail() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Long postId = mockPost.getId();
		CommentCreateReqDto request = new CommentCreateReqDto("Hello Comment");
		given(commentUsecase.createComment(eq(postId), eq(mockUser.getId()), any(CommentCreateReqDto.class)))
				.willThrow(new PostNotFoundException(postId));

		// when & then
		mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Post Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 수정 API - 성공")
	@WithMockCustomUser
	void updateCommentApi_success() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Comment mockComment = makeMockComment(mockPost, mockUser);

		Long userId = mockUser.getId();
		Long postId = mockPost.getId();
		Long commentId = mockComment.getId();
		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");
		CommentUpdateResDto response = new CommentUpdateResDto(
				mockUser.getId(), mockPost.getId(), 1L, "Hello Comment", CommentStatus.NORMAL,
				LocalDateTime.now(), LocalDateTime.now()
		);
		given(commentUsecase.updateComment(eq(postId), eq(commentId), eq(userId), any(CommentUpdateReqDto.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.userId").value(mockUser.getId()))
				.andExpect(jsonPath("$.content").value("Hello Comment"))
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 수정 API - 실패")
	@WithMockCustomUser
	void updateCommentApi_fail() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);

		Long userId = mockUser.getId();
		Long postId = mockPost.getId();
		Long commentId = 999L;
		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello");
		given(commentUsecase.updateComment(eq(postId), eq(commentId), eq(userId), any(CommentUpdateReqDto.class)))
				.willThrow(new CommentNotFoundException(commentId));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Comment Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Comment {" + commentId + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 상태 수정 API - 성공")
	@WithMockCustomUser
	void updateCommentStatusApi_success() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Comment mockComment = makeMockComment(mockPost, mockUser);

		Long userId = mockUser.getId();
		Long postId = 1L;
		Long commentId = mockComment.getId();
		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);
		CommentStatusUpdateResDto response = new CommentStatusUpdateResDto(postId, commentId, CommentStatus.DELETED);
		given(commentUsecase.updateCommentStatus(eq(postId), eq(commentId), eq(userId), any(CommentStatusUpdateReqDto.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}/status", postId, commentId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.commentId").value(commentId))
				.andExpect(jsonPath("$.status").value(CommentStatus.DELETED.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("댓글 상태 수정 API - 실패")
	@WithMockCustomUser
	void updateCommentStatusApi_fail() throws Exception {
		// given
		User mockUser = makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		Comment mockComment = makeMockComment(mockPost, mockUser);

		Long userId = mockUser.getId();
		Long postId = 1L;
		Long commentId = mockComment.getId();
		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.NORMAL);
		given(commentUsecase.updateCommentStatus(eq(postId), eq(commentId), eq(userId), any(CommentStatusUpdateReqDto.class)))
				.willThrow(new AccessDeniedException("USER {" + userId + "} has no permission to update Comment status " + commentId));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}/status", postId, commentId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				.andDo(print());
	}

}
