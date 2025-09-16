package dev.kyudong.back.notification.service;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.notification.repository.NotificationQuery;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final UserReaderService userReaderService;
	private final NotificationRepository notificationRepository;
	private final NotificationQuery notificationQuery;

	@Transactional(readOnly = true)
	public NotificationResDto findNotifications(final Long receiverId, Long cursorId) {
		log.debug("알림 목록을 조회합니다: receiverId={}, cursorId={}", receiverId, cursorId);

		List<NotificationQueryDto> notifications = notificationQuery.findNotifications(receiverId, cursorId);

		log.debug("알림 목록을 조회했습니다: receiverId={}", receiverId);
		return NotificationResDto.from(notifications);
	}

	@Transactional
	public void readNotification(final Long receiverId, final Long notificationId) {
		log.debug("알림을 조회합니다: receiverId={}, id={}", receiverId, notificationId);

		User receiver = userReaderService.getUserReference(receiverId);
		Notification notification = notificationRepository.findByIdAndReceiver(notificationId, receiver)
				.orElseThrow(() -> {
					log.warn("조회할 알림이 존재하지 않습니다: receiverId={}, id={}", receiverId, notificationId);
					return new NotificationNotFoundException(notificationId);
				});

		notification.read();
		log.debug("알림 조회를 완료했습니다: receiverId={}, id={}", receiverId, notificationId);
	}

	@Transactional
	public void deleteNotification(final Long receiverId, final Long notificationId) {
		log.debug("알림을 삭제합니다: id={}", notificationId);

		User receiver = userReaderService.getUserReference(receiverId);
		notificationRepository.deleteByIdAndReceiver(notificationId, receiver);

		log.debug("알림 삭제를 완료했습니다: id={}", notificationId);
	}

}
