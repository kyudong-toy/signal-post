package dev.kyudong.back.chat.api.dto.res;

import dev.kyudong.back.chat.domain.ChatMessage;
import dev.kyudong.back.chat.domain.MessageStatus;
import dev.kyudong.back.chat.domain.MessageType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ChatMessageDetailResDto(
		Long messageId,
		Long roomId,
		Long senderId,
		String senderUsername,
		String content,
		MessageType messageType,
		MessageStatus messageStatus,
		LocalDateTime createdAt
) {
	public static ChatMessageDetailResDto from(ChatMessage chatMessage) {
		return new ChatMessageDetailResDto(
				chatMessage.getId(),
				chatMessage.getChatRoom().getId(),
				chatMessage.getSender().getUser().getId(),
				chatMessage.getSender().getUser().getUsername(),
				chatMessage.getContent(),
				chatMessage.getMessageType(),
				chatMessage.getMessageStatus(),
				LocalDateTime.ofInstant(chatMessage.getCreatedAt(), ZoneOffset.UTC)
		);
	}
}
