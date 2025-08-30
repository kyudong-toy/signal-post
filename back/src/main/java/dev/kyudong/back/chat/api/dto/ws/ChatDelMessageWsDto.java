package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatMessageDeleteEvent;
import dev.kyudong.back.chat.domain.MessageStatus;
import dev.kyudong.back.chat.domain.MessageType;

public record ChatDelMessageWsDto(
		Long roomId,
		Long messageId,
		Long userId,
		MessageType messageType,
		MessageStatus messageStatus
) {
	public static ChatDelMessageWsDto from(ChatMessageDeleteEvent event) {
		return new ChatDelMessageWsDto(
				event.roomId(),
				event.messageId(),
				event.userId(),
				event.messageType(),
				event.messageStatus()
		);
	}
}
