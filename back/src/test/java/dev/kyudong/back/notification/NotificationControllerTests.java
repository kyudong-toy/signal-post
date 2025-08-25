package dev.kyudong.back.notification;

import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.notification.api.NotificationController;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.notification.service.NotificationService;
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

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.*;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
public class NotificationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private NotificationService notificationService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("rawPassword")
				.encodedPassword("encodedPassword")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static Notification makeMockNotification(User receiver, User sender, Long id) {
		Notification notification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl("/post/1")
				.type(NotificationType.POST)
				.build();
		ReflectionTestUtils.setField(notification, "id", id);
		ReflectionTestUtils.setField(notification, "createdAt", Instant.now());
		return notification;
	}

	@Test
	@DisplayName("알림 목록 조회 API - 성공: 첫 조회")
	@WithMockCustomUser
	void findNotificationsApi_success() throws Exception {
		// given
		User mockReceiver = makeMockUser("dxfxd", 1L);
		User mockSender = makeMockUser("zxqzz", 2L);

		NotificationDetailResDto notification1 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 1L));
		NotificationDetailResDto notification2 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 2L));
		NotificationDetailResDto notification3 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 3L));
		List<NotificationDetailResDto> content = List.of(notification1, notification2, notification3);
		NotificationResDto response = new NotificationResDto(2L, true, content);

		given(notificationService.findNotifications(1L, null, 10))
				.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/notification")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastNotificationId").hasJsonPath())
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.content").isArray())
				.andDo(print());
	}

	@Test
	@DisplayName("알림 목록 조회 API - 성공: 알림 아이디로 조회")
	@WithMockCustomUser
	void findNotificationsApi_success_withLastNotificationId() throws Exception {
		// given
		User mockReceiver = makeMockUser("dxfxd", 1L);
		User mockSender = makeMockUser("zxqzz", 2L);

		NotificationDetailResDto notification1 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 1L));
		NotificationDetailResDto notification2 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 2L));
		NotificationDetailResDto notification3 =
				NotificationDetailResDto.from(makeMockNotification(mockReceiver, mockSender, 3L));
		List<NotificationDetailResDto> content = List.of(notification1, notification2, notification3);
		NotificationResDto response = new NotificationResDto(2L, true, content);

		given(notificationService.findNotifications(1L, 2L, 10))
				.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/notification")
						.param("size", "10")
						.param("lastNotificationId", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastNotificationId").value(2L))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.content").isArray())
				.andDo(print());
	}

	@Test
	@DisplayName("알림 조회 API")
	@WithMockCustomUser
	void readNotificationApi_success() throws Exception {
		// given
		final Long receiverId = 1L;
		final Long notificationId = 1L;

		// when
		mockMvc.perform(patch("/api/v1/notification/{notificationId}", notificationId))
				.andExpect(status().isNoContent())
				.andDo(print());

		// then
		then(notificationService).should().readNotification(receiverId, notificationId);
	}

	@Test
	@DisplayName("알림 조회 API")
	@WithMockCustomUser
	void readNotificationApi_fail_notificationNotFound() throws Exception {
		// given
		final Long receiverId = 1L;
		final Long notificationId = 1L;

		willThrow(new NotificationNotFoundException(notificationId))
				.given(notificationService).readNotification(receiverId, notificationId);

		// when
		mockMvc.perform(patch("/api/v1/notification/{notificationId}", notificationId))
				.andExpect(status().isBadRequest())
				.andDo(print());

		// then
		then(notificationService).should().readNotification(receiverId, notificationId);
	}

	@Test
	@DisplayName("알림 삭제 API")
	@WithMockCustomUser
	void deleteNotification_success() throws Exception {
		// given
		final Long receiverId = 1L;
		final Long notificationId = 1L;

		// when
		mockMvc.perform(delete("/api/v1/notification/{notificationId}", notificationId))
				.andExpect(status().isNoContent())
				.andDo(print());

		// then
		then(notificationService).should().deleteNotification(receiverId, notificationId);
	}

}
