package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record NotificationContentDto(
		Long id,
		String redirectUrl,
		NotificationType type,
		LocalDateTime createdAt,
		NotificationContextDto context
) {
	public static NotificationContentDto from(NotificationQueryDto queryDto) {
		return new NotificationContentDto(
				queryDto.id(),
				queryDto.redirectUrl(),
				queryDto.type(),
				LocalDateTime.ofInstant(queryDto.createdAt(), ZoneOffset.UTC),
				NotificationContextDto.from(queryDto)
		);
	}
}
