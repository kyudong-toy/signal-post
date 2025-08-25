package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record NotificationDetailResDto(
		Long id,
		Long receiverId,
		Long senderId,
		String redirectUrl,
		NotificationType notificationType,
		LocalDateTime createdAt
) {
	public static NotificationDetailResDto from(Notification notification) {
		return new NotificationDetailResDto(
			notification.getId(),
			notification.getReceiver().getId(),
			notification.getSender().getId(),
			notification.getRedirectUrl(),
			notification.getType(),
			LocalDateTime.ofInstant(notification.getCreatedAt(), ZoneOffset.UTC)
		);
	}
}
