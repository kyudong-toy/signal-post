package dev.kyudong.back.notification.api.dto;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;

import java.time.Instant;

public record NotificationQueryDto(
		Long id,
		Long receiverId,
		NotificationType type,
		String redirectUrl,
		Instant createdAt,
		Long senderId,
		String senderName,
		Long postId,
		String postSubject,
		String postSummary,
		Long commentId,
		String commentContent
) {
	public static NotificationQueryDto createNotification(Notification notification) {
		return new NotificationQueryDto(
				notification.getId(),
				notification.getReceiver().getId(),
				notification.getType(),
				notification.getRedirectUrl(),
				notification.getCreatedAt(),
				notification.getSender().getId(),
				notification.getSender().getUsername(),
				notification.getPost().getId(),
				notification.getPost().getSubject(),
				notification.getPost().getContent(),
				null,
				null
		);
	}
}
