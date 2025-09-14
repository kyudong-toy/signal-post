package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;

public record NotificationSenderDto(
		Long senderId,
		String senderName
) {
	public static NotificationSenderDto from(NotificationQueryDto queryDto) {
		return new NotificationSenderDto(
				queryDto.senderId(),
				queryDto.senderName()
		);
	}
}
