package dev.kyudong.back.interaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.interaction.api.dto.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.dto.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.InteractionType;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.repository.InteractionRepository;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

public class InteractionIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InteractionRepository interactionRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser() {
		User newUser = User.builder()
				.username("TestUser!")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		return userRepository.save(newUser);
	}

	private Post createTestPost(User user) throws JsonProcessingException {
		Post newPost = Post.create("제목", createMockTiptapContent());
		user.addPost(newPost);
		return postRepository.save(newPost);
	}

	private String createMockTiptapContent() throws JsonProcessingException {
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

	private Interaction createTestInteraction(User user, Post post) {
		Interaction interaction = Interaction.builder()
				.user(user)
				.targetId(post.getId())
				.targetType(TargetType.POST)
				.interactionType(InteractionType.LAUGH)
				.build();
		return interactionRepository.save(interaction);
	}

	@Test
	@DisplayName("상호작용 요청")
	void doInteraction() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);

		InteractionReqDto request = new InteractionReqDto(InteractionType.LAUGH);

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/interaction/{targetType}/{targetId}", TargetType.POST.name(), post.getId())
									.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
									.contentType(MediaType.APPLICATION_JSON.toString())
									.content(objectMapper.writeValueAsString(request)))
							.andDo(print())
							.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		InteractionResDto response = objectMapper.readValue(responseBody, InteractionResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.userId()).isEqualTo(user.getId());
		assertThat(response.targetType()).isEqualTo(TargetType.POST);
		assertThat(response.interactionType()).isEqualTo(InteractionType.LAUGH);

		Optional<Interaction> interactionOptional = interactionRepository.findById(response.id());
		assertThat(interactionOptional).isPresent();

		Interaction interaction = interactionOptional.get();
		assertThat(interaction.getTargetId()).isEqualTo(post.getId());
	}

	@Test
	@DisplayName("상호작용 삭제")
	void deleteInteractionApi_success() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Interaction interaction = createTestInteraction(user, post);

		// when
		mockMvc.perform(delete("/api/v1/interaction/{targetType}/{targetId}", TargetType.POST.name(), post.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user)))
				.andExpect(status().isNoContent())
				.andDo(print());

		// then
		Optional<Interaction> interactionOptional = interactionRepository.findById(interaction.getId());
		assertThat(interactionOptional).isNotPresent();
	}

}
