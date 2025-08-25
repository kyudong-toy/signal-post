package dev.kyudong.back.notification;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.scheduler.NotificationScheduler;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationScedulerTests {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private NotificationScheduler notificationScheduler;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static Notification makeMockNotification(User receiver, User sender, Long id) {
		Notification mockNotification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl("/example")
				.type(NotificationType.POST)
				.build();
		ReflectionTestUtils.setField(mockNotification, "id", id);
		ReflectionTestUtils.setField(mockNotification, "createdAt", Instant.now().minus(366, ChronoUnit.DAYS));
		return mockNotification;
	}

	@Test
	void cleanupOrphanedNotification_success() {
		// given
		User receiver = makeMockUser("dkdkqwn", 1L);
		User sender = makeMockUser("wqqwq", 2L);

		List<Notification> oldNotifications = Arrays.asList(
				makeMockNotification(receiver, sender, 1L),
				makeMockNotification(receiver, sender, 2L)
		);
		given(notificationRepository.findByCreatedAtBefore(any(Instant.class)))
				.willReturn(oldNotifications);

		// when
		notificationScheduler.cleanupOrphanedNotification();

		// then
		then(notificationRepository).should().deleteAll(oldNotifications);
	}

}
