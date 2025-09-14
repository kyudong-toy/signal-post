package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record NotificationDetailResDto(
		NotificationSenderDto sender,
		NotificationContentDto content
) {
	public static NotificationDetailResDto from(NotificationQueryDto queryDto) {
		return new NotificationDetailResDto(
			NotificationSenderDto.from(queryDto),
			NotificationContentDto.from(queryDto)
		);
	}
}
