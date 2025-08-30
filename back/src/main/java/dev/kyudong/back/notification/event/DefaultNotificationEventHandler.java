package dev.kyudong.back.notification.event;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.handler.NotificationWebSocketHandler;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.utils.RedirectUrlCreator;
import dev.kyudong.back.post.api.dto.event.PostCreateNotification;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultNotificationEventHandler implements NotificationEventHandler {

	private final NotificationRepository notificationRepository;
	private final FollowRepository followRepository;
	private final UserRepository userRepository;
	private final NotificationWebSocketHandler notificationWebSocketHandler;
	private final RedirectUrlCreator redirectUrlCreator;

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handlePostCreateEvent(PostCreateNotification event) {
		final Long postId = event.postId();
		log.info("게시글 생성 이벤트 수신완료, 알림 이벤트를 시작합니다: postId={}", postId);

		User sender = userRepository.getReferenceById(event.senderId());
		List<User> followers = followRepository.findByFollowingWithFollower(sender).stream()
				.map(Follow::getFollower)
				.toList();

		if (followers.isEmpty()) {
			log.debug("팔로워가 없어 알림 생성을 중지합니다: senderId={}, postId={}", event.senderId(), postId);
			return;
		}

		String redirectUrl = redirectUrlCreator.createPostUrl(event.postId());
		List<Notification> newNotifications = followers.stream()
				.map(receiver ->
					Notification.builder()
								.receiver(receiver)
								.sender(sender)
								.redirectUrl(redirectUrl)
								.type(NotificationType.POST)
								.build()
				)
				.toList();
		List<Notification> savedNotifications = notificationRepository.saveAll(newNotifications);
		log.info("팔로워 {}에게 알림을 생성했습니다: postId={}", savedNotifications.size(), postId);

		savedNotifications.forEach(notification -> {
			NotificationDetailResDto notificationDetailResDto = NotificationDetailResDto.from(notification);
			notificationWebSocketHandler.sendNotificationToUser(notificationDetailResDto.receiverId(), notificationDetailResDto);
		});
	}

}
