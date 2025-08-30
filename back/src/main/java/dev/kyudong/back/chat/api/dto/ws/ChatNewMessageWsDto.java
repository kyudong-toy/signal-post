package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatMessageCreateEvent;
import dev.kyudong.back.chat.domain.MessageStatus;
import dev.kyudong.back.chat.domain.MessageType;

import java.time.LocalDateTime;

public record ChatNewMessageWsDto(
		Long messageId,
		Long roomId,
		Long senderId,
		String senderUsername,
		String content,
		MessageType messageType,
		MessageStatus messageStatus,
		LocalDateTime createdAt
) {
	public static ChatNewMessageWsDto from(ChatMessageCreateEvent event) {
		return new ChatNewMessageWsDto(
				event.messageId(),
				event.roomId(),
				event.senderId(),
				event.senderUsername(),
				event.content(),
				event.messageType(),
				event.messageStatus(),
				event.createdAt()
		);
	}
}
