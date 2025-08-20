package dev.kyudong.back.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.api.PostController;
import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.PostStatus;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.service.PostService;
import dev.kyudong.back.security.WithMockCustomUser;
import dev.kyudong.back.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
public class PostControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PostService postService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("게시글 조회 API - 성공")
	void findPostByIdApi_success() throws Exception {
		// given
		final Long postId = 1L;
		PostDetailResDto response = new PostDetailResDto(postId, 1L, "Test", "Hello World!", PostStatus.NORMAL, LocalDateTime.now(), LocalDateTime.now());
		when(postService.findPostById(eq(postId))).thenReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 조회 API - 실패")
	void findPostByIdApi_fail() throws Exception {
		// given
		final Long postId = 999L;
		when(postService.findPostById(eq(postId)))
				.thenThrow(new PostNotFoundException(postId));

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Post Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Post {" + postId + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API - 성공")
	@WithMockCustomUser
	void createPostApi_success() throws Exception {
		// given
		final Long postId = 1L;
		PostCreateReqDto request = new PostCreateReqDto("Test", "Hello World!", new HashSet<>());
		PostCreateResDto response = new PostCreateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		when(postService.createPost(eq(postId), any(PostCreateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/posts")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API - 실패 : 토큰은 유효하지만 DB에 작성자가 없는 경우")
	@WithMockCustomUser(id = 999L)
	void createPostApi_fail() throws Exception {
		// given
		final Long userId = 999L;
		PostCreateReqDto request = new PostCreateReqDto("Test", "Hello World!", new HashSet<>());
		when(postService.createPost(eq(userId), any(PostCreateReqDto.class)))
				.thenThrow(new UserNotFoundException(userId));

		// when & then
		mockMvc.perform(post("/api/v1/posts")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("User Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("User {" + userId + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 수정 API - 성공")
	@WithMockCustomUser
	void updatePostApi_success() throws Exception {
		// given
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto("Test", "Hello World!", new HashSet<>(), new HashSet<>());
		PostUpdateResDto response = new PostUpdateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		when(postService.updatePost(eq(postId), eq(1L), any(PostUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/update", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 수정 API - 실패 : 게시글 수정 권한 없음")
	@WithMockCustomUser
	void updatePostApi_fail() throws Exception {
		// given
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto( "Test", "Hello World!", new HashSet<>(), new HashSet<>());
		when(postService.updatePost(eq(postId), eq(1L), any(PostUpdateReqDto.class)))
				.thenThrow(new InvalidAccessException("User {" + 1L + "} has no permission to update post " + postId));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/update", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Access Denied"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("User {" + 1L + "} has no permission to update post " + postId))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 상태 수정 API")
	@WithMockCustomUser
	void updatePostStatusApi_success() throws Exception {
		// given
		final Long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);
		PostStatusUpdateResDto response = new PostStatusUpdateResDto(postId, PostStatus.DELETED);
		when(postService.updatePostStatus(eq(postId), eq(1L), any(PostStatusUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/status", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(1L))
				.andExpect(jsonPath("$.status").value(PostStatus.DELETED.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 상태 수정 API - 실패 : 게시글 수정 권한 없음")
	@WithMockCustomUser
	void updatePostStatusApi_fail() throws Exception {
		// given
		final Long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);
		when(postService.updatePostStatus(eq(postId), eq(1L), any(PostStatusUpdateReqDto.class)))
				.thenThrow(new InvalidAccessException("User {" + 1L + "} has no permission to update post status " + postId));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/status", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Access Denied"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("User {" + 1L + "} has no permission to update post status " + postId))
				.andDo(print());
	}

}
