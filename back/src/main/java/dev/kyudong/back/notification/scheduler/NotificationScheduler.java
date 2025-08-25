package dev.kyudong.back.notification.scheduler;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final NotificationRepository notificationRepository;

	@Scheduled(cron = "0 30 3 * * *")
	@Transactional
	public void cleanupOrphanedNotification() {
		log.info("오래된 알림을 정리를 시작합니다........");

		// 현재 시점에서 1년 이전의 알림이 기준
		Instant threshold = Instant.now().minus(366, ChronoUnit.DAYS);

		List<Notification> orphanedNotification = notificationRepository.findByCreatedAtBefore(threshold);

		if (orphanedNotification.isEmpty()) {
			log.info("정리할 알림이 없습니다.........");
			return;
		}

		int notificationSize = orphanedNotification.size();
		log.info("{}개의 알림을 정리합니다!", notificationSize);

		notificationRepository.deleteAll(orphanedNotification);
		log.info("오래된 알림 {}개의 정리가 완료되었습니다", notificationSize);
	}

}
