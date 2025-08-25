package dev.kyudong.back.notification.service;

import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final UserRepository userRepository;
	private final NotificationRepository notificationRepository;

	@Transactional(readOnly = true)
	public NotificationResDto findNotifications(final Long receiverId, Long lastNotificationId, int size) {
		log.debug("알림 목록을 조회합니다: receiverId={}, lastNotificationId={}, size={}", receiverId, lastNotificationId, size);
		User receiver = userRepository.getReferenceById(receiverId);
		PageRequest pageRequest = PageRequest.of(0, size);

		Slice<Notification> notifications = (lastNotificationId == null)
				? notificationRepository.findNotificationByReceiver(receiver, pageRequest)
				: notificationRepository.findNotificationByReceiver(receiver, lastNotificationId, pageRequest);

		log.info("알림 목록을 조회했습니다: receiverId={}", receiverId);
		return NotificationResDto.from(notifications);
	}

	@Transactional
	public void readNotification(final Long receiverId, final Long notificationId) {
		log.debug("알림을 조회합니다: receiverId={}, notificationId={}", receiverId, notificationId);

		User receiver = userRepository.getReferenceById(receiverId);
		Notification notification = notificationRepository.findByIdAndReceiver(notificationId, receiver)
				.orElseThrow(() -> {
					log.warn("조회할 알림이 존재하지 않습니다: receiverId={}, notificationId={}", receiverId, notificationId);
					return new NotificationNotFoundException(notificationId);
				});

		notification.read();
		log.info("알림 조회를 완료했습니다: receiverId={}, notificationId={}", receiverId, notificationId);
	}

	@Transactional
	public void deleteNotification(final Long receiverId, final Long notificationId) {
		log.debug("알림을 삭제합니다: notificationId={}", notificationId);

		User receiver = userRepository.getReferenceById(receiverId);
		notificationRepository.deleteByIdAndReceiver(notificationId, receiver);

		log.info("알림 삭제를 완료했습니다: notificationId={}", notificationId);
	}

}
