package dev.kyudong.back.notification.service;

import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.notification.api.NotificationController;
import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
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
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("tester")
				.rawPassword("rawPassword")
				.encodedPassword("encodedPassword")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	@Test
	@DisplayName("알림 목록 조회 API - 성공: 첫 조회")
	@WithMockCustomUser
	void findNotificationsApi_success() throws Exception {
		// given
		User mockReceiver = makeMockUser();
		List<NotificationQueryDto> notifications = List.of(
				new NotificationQueryDto(
						1L, 2L, NotificationType.POST,
						"/post/1", Instant.now(),
						1L, "발신자1",
						1L, "게시글제목", null,
						null, null
				),
				new NotificationQueryDto(
						1L, 3L, NotificationType.POST,
						"/post/2", Instant.now(),
						1L, "발신자1",
						1L, "게시글제목2", null,
						null, null
				),
				new NotificationQueryDto(
						1L, 2L, NotificationType.COMMENT,
						"/post/1", Instant.now(),
						1L, "발신자1",
						1L, null, null,
						1L, "댓글내용"
				),
				new NotificationQueryDto(
						1L, 2L, NotificationType.COMMENT,
						"/post/1", Instant.now(),
						2L, "발신자2",
						1L, null, null,
						1L, "댓글내용2"
				)
		);
		NotificationResDto response = NotificationResDto.from(notifications);
		given(notificationService.findNotifications(mockReceiver.getId(), null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/notification"))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("알림 목록 조회 API - 성공: 첫 조회")
	@WithMockCustomUser
	void findNotificationsApi_success_withCursor() throws Exception {
		// given
		User mockReceiver = makeMockUser();
		final Long cursorId = 20L;
		List<NotificationQueryDto> notifications = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			notifications.add(
					new NotificationQueryDto(
							(long) i, 2L, NotificationType.POST,
							"/post/1", Instant.now(),
							1L, "발신자1",
							1L, "게시글제목", null,
							null, null
					)
			);
		}
		NotificationResDto response = NotificationResDto.from(notifications);
		given(notificationService.findNotifications(mockReceiver.getId(), cursorId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/notification"))
				.andExpect(status().isOk())
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
