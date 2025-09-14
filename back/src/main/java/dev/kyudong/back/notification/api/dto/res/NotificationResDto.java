package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;

import java.util.List;

public record NotificationResDto(
		Long cursorId,
		boolean hasNext,
		List<NotificationDetailResDto> contents
) {
	public static NotificationResDto from(List<NotificationQueryDto> queryDtos) {
		List<NotificationDetailResDto> content = queryDtos.stream()
				.map(NotificationDetailResDto::from)
				.limit(20)
				.toList();

		boolean hasNext = queryDtos.size() > 20;

		Long cursorId = null;
		if (hasNext) {
			cursorId = content.get(content.size() - 1).content().id();
		}

		return new NotificationResDto(cursorId, hasNext, content);
	}
}
