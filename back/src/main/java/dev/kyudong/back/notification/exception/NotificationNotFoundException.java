package dev.kyudong.back.notification.exception;

public class NotificationNotFoundException extends RuntimeException {
	public NotificationNotFoundException(Long notificationId) {
		super(String.format("조회를 요청한 알림이 존재하지 않습니다: notificationId=%d", notificationId));
	}
}
