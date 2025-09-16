package dev.kyudong.back.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser(String username) {
		User newUser = User.builder()
				.username(username)
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


	private Notification createTestNotification(User receiver, User sender, String redirectUrl) {
		Notification newNotification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl(redirectUrl)
				.type(NotificationType.POST)
				.build();
		return notificationRepository.save(newNotification);
	}

	@Test
	@DisplayName("알림 목록 조회")
	void findNotifications() throws Exception {
		// given
		User receiver = createTestUser("receiver");
		User sender = createTestUser("following");

		Post post = createTestPost(sender);

		String redirectUrl = "/post/" + post.getId();
		for (int i = 0; i < 100; i++) {
			createTestNotification(receiver, sender, redirectUrl);
		}

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/notification")
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(receiver)))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		NotificationResDto response = objectMapper.readValue(responseBody, NotificationResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isTrue();

		Optional<Notification> optionalNotification = notificationRepository.findById(response.contents().get(0).content().id());
		assertThat(optionalNotification).isPresent();
	}

	@Test
	@DisplayName("알림 조회")
	void readNotification() throws Exception {
		// given
		User receiver = createTestUser("receiver");
		User sender = createTestUser("following");

		Post post = createTestPost(sender);
		Notification notification = createTestNotification(receiver, sender, "/post/" + post.getId());

		// when
		mockMvc.perform(patch("/api/v1/notification/{notificationId}", notification.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(receiver)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		Optional<Notification> optionalNotification = notificationRepository.findById(notification.getId());
		assertThat(optionalNotification).isPresent();

		Notification savedNotification = optionalNotification.get();
		assertThat(savedNotification.isRead()).isTrue();
	}

	@Test
	@DisplayName("알림 삭제")
	void deleteNotification() throws Exception {
		// given
		User receiver = createTestUser("receiver");
		User sender = createTestUser("following");

		Post post = createTestPost(sender);
		Notification notification = createTestNotification(receiver, sender, "/post/" + post.getId());

		// when
		mockMvc.perform(delete("/api/v1/notification/{notificationId}", notification.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(receiver)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		Optional<Notification> optionalNotification = notificationRepository.findById(notification.getId());
		assertThat(optionalNotification).isNotPresent();
	}

}
