package dev.kyudong.back.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidAccessException;
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
import dev.kyudong.back.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PostService postService;

	@Test
	@DisplayName("게시글 조회 API - 성공")
	void findPostByIdApi_success() throws Exception {
		// given
		long postId = 1L;
		PostDetailResDto response = new PostDetailResDto(postId, 1L, "Test", "Hello World!", PostStatus.NORMAL, LocalDateTime.now(), LocalDateTime.now());
		when(postService.findPostById(eq(postId))).thenReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/post/{postId}", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 조회 API - 실패 : 요청한 게시글이 존재하지 않음.")
	void findPostByIdApi_fail() throws Exception {
		// given
		long postId = 999L;
		when(postService.findPostById(eq(postId)))
				.thenThrow(new PostNotFoundException("Post {" + postId + "} Not Found"));

		// when & then
		mockMvc.perform(get("/api/v1/post/{postId}", postId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Post Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Post {" + postId + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API - 성공")
	void createPostApi_success() throws Exception {
		// given
		long postId = 1L;
		PostCreateReqDto request = new PostCreateReqDto(1L, "Test", "Hello World!");
		PostCreateResDto response = new PostCreateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		when(postService.createPost(any(PostCreateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/post")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API - 실패 : 작성자의 정보가 조회가 안됨")
	void createPostApi_fail() throws Exception {
		// given
		PostCreateReqDto request = new PostCreateReqDto(1L, "Test", "Hello World!");
		when(postService.createPost(any(PostCreateReqDto.class)))
				.thenThrow(new UserNotFoundException("User {" + request.userId() + "} Not Found"));

		// when & then
		mockMvc.perform(post("/api/v1/post")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("User Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("User {" + request.userId() + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 수정 API - 성공")
	void updatePostApi_success() throws Exception {
		// given
		long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(1L, "Test", "Hello World!");
		PostUpdateResDto response = new PostUpdateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		when(postService.updatePost(eq(postId), any(PostUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/post/{postId}/update", postId)
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
	void updatePostApi_fail() throws Exception {
		// given
		long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(1L, "Test", "Hello World!");
		when(postService.updatePost(eq(postId), any(PostUpdateReqDto.class)))
				.thenThrow(new InvalidAccessException("User {" + request.userId() + "} has no permission to update post " + postId));

		// when & then
		mockMvc.perform(patch("/api/v1/post/{postId}/update", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Access Denied"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("User {" + request.userId() + "} has no permission to update post " + postId))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 상태 수정 API")
	void updatePostStatusApi_success() throws Exception {
		// given
		long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(1L, PostStatus.DELETED);
		PostStatusUpdateResDto response = new PostStatusUpdateResDto(postId, PostStatus.DELETED);
		when(postService.updatePostStatus(eq(postId), any(PostStatusUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/post/{postId}/status", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(1L))
				.andExpect(jsonPath("$.status").value(PostStatus.DELETED.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 상태 수정 API - 실패 : 게시글 수정 권한 없음")
	void updatePostStatusApi_fail() throws Exception {
		// given
		long postId = 1L;
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(1L, PostStatus.DELETED);
		when(postService.updatePostStatus(eq(postId), any(PostStatusUpdateReqDto.class)))
				.thenThrow(new InvalidAccessException("User {" + request.userId() + "} has no permission to update post status " + postId));

		// when & then
		mockMvc.perform(patch("/api/v1/post/{postId}/status", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.title").value("Access Denied"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("User {" + request.userId() + "} has no permission to update post status " + postId))
				.andDo(print());
	}

}
