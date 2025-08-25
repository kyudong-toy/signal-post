package dev.kyudong.back.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class NotificationIntegrationTests {

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

	private Post createTestPost(User user) {
		Post newPost = Post.builder()
				.subject("Test")
				.content("Hello World!")
				.build();
		user.addPost(newPost);
		return postRepository.save(newPost);
	}

	private Notification createMockNotification(User receiver, User sender, String redirectUrl) {
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
		createMockNotification(receiver, sender, "/post/" + post.getId());

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/notification")
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(receiver))
								.param("size", "10"))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		NotificationResDto response = objectMapper.readValue(responseBody, NotificationResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.content().get(0).redirectUrl()).isEqualTo("/post/" + post.getId());
		assertThat(response.hasNext()).isFalse();

		Optional<Notification> optionalNotification = notificationRepository.findById(response.content().get(0).id());
		assertThat(optionalNotification).isPresent();
	}

	@Test
	@DisplayName("알림 조회")
	void readNotification() throws Exception {
		// given
		User receiver = createTestUser("receiver");
		User sender = createTestUser("following");

		Post post = createTestPost(sender);
		Notification notification = createMockNotification(receiver, sender, "/post/" + post.getId());

		// when
		mockMvc.perform(patch("/api/v1/notification/{notificationId}", notification.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(receiver)))
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
		Notification notification = createMockNotification(receiver, sender, "/post/" + post.getId());

		// when
		mockMvc.perform(delete("/api/v1/notification/{notificationId}", notification.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(receiver)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		Optional<Notification> optionalNotification = notificationRepository.findById(notification.getId());
		assertThat(optionalNotification).isNotPresent();
	}

}
