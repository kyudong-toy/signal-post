package dev.kyudong.back.interaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.interaction.api.InteractionController;
import dev.kyudong.back.interaction.api.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.InteractionType;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.service.InteractionService;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.exception.CommentNotFoundException;
import dev.kyudong.back.security.WithMockCustomUser;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InteractionController.class)
@Import(SecurityConfig.class)
public class InteractionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private InteractionService interactionService;

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
		ReflectionTestUtils.setField(mockUser, "id", 2L);
		return mockUser;
	}

	private static Post makeMockPost(User mockUser) {
		Post mockPost = Post.builder()
				.subject("제목")
				.content("본문")
				.build();
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	@Test
	@DisplayName("상호작용 요청 API - 성공")
	@WithMockCustomUser
	void doInteractionApi_success() throws Exception {
		// given
		User mockUser= makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		InteractionReqDto request = new InteractionReqDto(InteractionType.LAUGH);
		InteractionResDto response = new InteractionResDto(1L, 1L, TargetType.POST, mockPost.getId(), InteractionType.LAUGH);
		given(interactionService.doInteraction(1L, TargetType.POST, mockPost.getId(), request))
				.willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/interaction/{targetType}/{targetId}", TargetType.POST.name(), mockPost.getId())
						.contentType(org.junit.jupiter.api.extension.MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1L))
				.andExpect(jsonPath("$.targetType").value(TargetType.POST.name()))
				.andExpect(jsonPath("$.targetId").value(mockPost.getId()))
				.andExpect(jsonPath("$.interactionType").value(InteractionType.LAUGH.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("팔로우 요청 API - 실패")
	@WithMockCustomUser
	void doInteractionApi_fail_commentNotFound() throws Exception {
		// given
		User mockUser= makeMockUser();
		Post mockPost = makeMockPost(mockUser);
		InteractionReqDto request = new InteractionReqDto(InteractionType.SAD);
		given(interactionService.doInteraction(1L, TargetType.POST, mockPost.getId(), request))
				.willThrow(CommentNotFoundException.class);

		// when & then
		mockMvc.perform(post("/api/v1/interaction/{targetType}/{targetId}", TargetType.POST.name(), mockPost.getId())
						.contentType(org.junit.jupiter.api.extension.MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Comment Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andDo(print());
	}

	@Test
	@DisplayName("상호작용 요청 API - 성공")
	@WithMockCustomUser
	void deleteInteractionApi_success() throws Exception {
		// given
		User mockUser= makeMockUser();
		Post mockPost = makeMockPost(mockUser);

		// when & then
		mockMvc.perform(delete("/api/v1/interaction/{targetType}/{targetId}", TargetType.POST.name(), mockPost.getId()))
				.andExpect(status().isNoContent())
				.andDo(print());

	}

}
