package dev.kyudong.back.notification.service;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.notification.repository.NotificationQuery;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

public class NotificationServiceTests extends UnitTestBase {

	@Mock
	private NotificationRepository notificationRepository;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationQuery notificationQuery;

	@Mock
	private UserReaderService userReaderService;

	@Nested
	@DisplayName("알림 목록 조회")
	class FindNotification {

		@Test
		@DisplayName("성공: 첫 조회")
		void success() {
			// given
			final Long receiverId = 1L;
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
			given(notificationQuery.findNotifications(receiverId, null)).willReturn(notifications);

			// when
			NotificationResDto response = notificationService.findNotifications(receiverId, null);

			// then
			assertThat(response).isNotNull();
			assertThat(response.hasNext()).isEqualTo(false);
			assertThat(response.cursorId()).isNull();
			then(notificationQuery).should().findNotifications(receiverId, null);
		}

		@Test
		@DisplayName("성공: 커서로 다음 조회")
		void success_withCursor() {
			// given
			final Long receiverId = 1L;
			final Long cursorId = 20L;
			List<NotificationQueryDto> mockNotifications = new ArrayList<>();
			for (int i = 1; i <= 100; i++) {
				mockNotifications.add(
						new NotificationQueryDto(
								(long) i, 2L, NotificationType.POST,
								"/post/1", Instant.now(),
								1L, "발신자1",
								1L, "게시글제목", null,
								null, null
						)
				);
			}
			given(notificationQuery.findNotifications(receiverId, cursorId)).willReturn(mockNotifications);

			// when
			NotificationResDto response = notificationService.findNotifications(receiverId, cursorId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.hasNext()).isEqualTo(true);
			assertThat(response.cursorId()).isNotNull();
			then(notificationQuery).should().findNotifications(receiverId, cursorId);
		}

	}

	@Nested
	@DisplayName("알림 조회")
	class ReadNotification {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			final Long receiverId = 1L;
			User mockReceivcer = createMockUser("sdfdsz", receiverId);
			given(userReaderService.getUserReference(mockReceivcer.getId())).willReturn(mockReceivcer);

			User mockSender = createMockUser("dkxzzz", 2L);
			Notification mockNotification = createMockNotification(mockReceivcer, mockSender);

			final Long notificationId = 1L;
			given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.of(mockNotification));

			// when
			notificationService.readNotification(receiverId, notificationId);

			// then
			assertThat(mockNotification.isRead()).isTrue();
			then(userReaderService).should().getUserReference(receiverId);
			then(notificationRepository).should().findByIdAndReceiver(notificationId, mockReceivcer);
		}

		@Test
		@DisplayName("실패: 알림을 찾을 수 없음")
		void readNotification_fail_notificationNotFound() {
			// given
			final Long receiverId = 1L;
			User mockReceivcer = createMockUser("sdfdsz", receiverId);
			given(userReaderService.getUserReference(mockReceivcer.getId())).willReturn(mockReceivcer);

			final Long notificationId = 1L;
			given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> notificationService.readNotification(receiverId, notificationId))
					.isInstanceOf(NotificationNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("알림 삭제")
	class DeleteNotification {

		@Test
		@DisplayName("알림 삭제 - 성공")
		void deleteNotification_success() {
			// given
			final Long receiverId = 1L;
			User mockReceivcer = createMockUser("sdfdsz", receiverId);
			given(userReaderService.getUserReference(mockReceivcer.getId())).willReturn(mockReceivcer);

			final Long notificationId = 1L;
			User mockSender = createMockUser("dkxzzz", 2L);
			Notification mockNotification = createMockNotification(mockReceivcer, mockSender);
			given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.of(mockNotification));

			// when
			notificationService.readNotification(receiverId, notificationId);

			// then
			assertThat(mockNotification.isRead()).isTrue();
			then(userReaderService).should().getUserReference(receiverId);
			then(notificationRepository).should().findByIdAndReceiver(notificationId, mockReceivcer);
		}

	}

}
