package dev.kyudong.back.post.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.adapter.in.web.PostController;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
import dev.kyudong.back.user.exception.UserNotFoundException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
public class PostControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private PostUsecase postUsecase;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	private static Map<String, Object> createMockTiptapContent() {
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
	@DisplayName("사용자 게시글 조회 API - 성공")
	@WithMockCustomUser(id = 999L)
	void findPostByIdApi_withUser_success() throws Exception {
		// given
		final Long userId = 999L;
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		final Long postId = 1L;
		PostDetailResDto response = new PostDetailResDto(
				postId,
				1L,
				981L,
				"Hello World!",
				"contents",
				PostStatus.NORMAL,
				LocalDateTime.now(),
				LocalDateTime.now()
		);
		given(postUsecase.findPostByIdWithUser(eq(userId), eq(postId))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.cookie(cookie))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("게스트 게시글 조회 API - 성공")
	void findPostByIdApi_withGuest_success() throws Exception {
		// given
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		final Long postId = 1L;
		PostDetailResDto response = new PostDetailResDto(
				postId,
				1L,
				981L,
				"Hello World!",
				"test",
				PostStatus.NORMAL,
				LocalDateTime.now(),
				LocalDateTime.now()
		);
		given(postUsecase.findPostByIdWithGuest(eq(guestId), eq(postId))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.cookie(cookie))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 게시글 조회 API - 실패")
	@WithMockCustomUser(id = 999L)
	void findPostByIdApi_withUser_fail() throws Exception {
		// given
		final Long userId = 999L;
		final Long postId = 999L;
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		given(postUsecase.findPostByIdWithUser(eq(userId), eq(postId))).willThrow(new PostNotFoundException(postId));

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.cookie(cookie))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andDo(print());
	}

	@Test
	@DisplayName("게스트 게시글 조회 API - 실패")
	void findPostByIdApi_withGuest_fail() throws Exception {
		// given
		final Long postId = 999L;
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		given(postUsecase.findPostByIdWithGuest(eq(guestId), eq(postId))).willThrow(new PostNotFoundException(postId));

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.cookie(cookie))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API - 성공")
	@WithMockCustomUser
	void createPostApi_success() throws Exception {
		// given
		final Long postId = 1L;
		Object content = createMockTiptapContent();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				content,
				new HashSet<>(),
				new HashSet<>()
		);
		PostCreateResDto response = new PostCreateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		given(postUsecase.createPost(eq(postId), any(PostCreateReqDto.class))).willReturn(response);

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
		Object content = createMockTiptapContent();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				content,
				new HashSet<>(),
				new HashSet<>()
		);
		given(postUsecase.createPost(eq(userId), any(PostCreateReqDto.class))).willThrow(new UserNotFoundException(userId));

		// when & then
		mockMvc.perform(post("/api/v1/posts")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("USER Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 수정 API - 성공")
	@WithMockCustomUser
	void updatePostApi_success() throws Exception {
		// given
		final Long postId = 1L;
		PostUpdateReqDto request = new PostUpdateReqDto(
				"Test",
				createMockTiptapContent(),
				new HashSet<>(),
				new HashSet<>()
		);
		PostUpdateResDto response = new PostUpdateResDto(postId, "Test", "Hello World!", LocalDateTime.now(), LocalDateTime.now());
		given(postUsecase.updatePost(eq(postId), eq(1L), any(PostUpdateReqDto.class))).willReturn(response);

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
		PostUpdateReqDto request = new PostUpdateReqDto(
				"Test",
				createMockTiptapContent(),
				new HashSet<>(),
				new HashSet<>()
		);
		given(postUsecase.updatePost(eq(postId), eq(1L), any(PostUpdateReqDto.class)))
				.willThrow(new AccessDeniedException(""));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/update", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
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
		given(postUsecase.updatePostStatus(eq(postId), eq(1L), any(PostStatusUpdateReqDto.class))).willReturn(response);

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
		given(postUsecase.updatePostStatus(eq(postId), eq(1L), any(PostStatusUpdateReqDto.class)))
				.willThrow(new AccessDeniedException(""));

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/status", postId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden())
				.andDo(print());
	}

}
