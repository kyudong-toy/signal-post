package dev.kyudong.back.notification.sceduler;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.scheduler.NotificationScheduler;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;

public class NotificationScedulerTests extends UnitTestBase {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationScheduler notificationScheduler;

	@Test
	@DisplayName("오래된 알림 삭제 스케쥴러 - 성공")
	void cleanupOrphanedNotification_success() {
		// given
		User receiver = createMockUser("dkdkqwn", 1L);
		User sender = createMockUser("wqqwq", 2L);

		List<Notification> oldNotifications = Arrays.asList(
				createMockNotification(receiver, sender, 1L),
				createMockNotification(receiver, sender, 2L)
		);
		given(notificationRepository.findByCreatedAtBefore(any(Instant.class)))
				.willReturn(oldNotifications);

		// when
		notificationScheduler.cleanupOrphanedNotification();

		// then
		then(notificationRepository).should().deleteAll(oldNotifications);
	}

}
