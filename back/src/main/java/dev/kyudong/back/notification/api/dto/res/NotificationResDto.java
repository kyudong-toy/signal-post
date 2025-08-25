package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.domain.Notification;
import org.springframework.data.domain.Slice;

import java.util.List;

public record NotificationResDto(
		Long lastNotificationId,
		boolean hasNext,
		List<NotificationDetailResDto> content
) {
	public static NotificationResDto from(Slice<Notification> notifications) {
		List<NotificationDetailResDto> content = notifications.getContent().stream()
				.map(NotificationDetailResDto::from)
				.toList();

		Long lastNotificationId = null;
		boolean hasNext = notifications.hasNext();

		if (hasNext) {
			lastNotificationId = content.get(content.size() - 1 ).id();
		}

		return new NotificationResDto(lastNotificationId, hasNext, content);
	}
}
