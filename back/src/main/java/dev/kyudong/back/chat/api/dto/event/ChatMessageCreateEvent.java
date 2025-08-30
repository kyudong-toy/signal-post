package dev.kyudong.back.chat.api.dto.event;

import dev.kyudong.back.chat.domain.*;
import dev.kyudong.back.user.domain.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

public record ChatMessageCreateEvent(
		Long messageId,
		Long roomId,
		Long senderId,
		String senderUsername,
		String content,
		MessageType messageType,
		MessageStatus messageStatus,
		LocalDateTime createdAt,
		Set<ChatMember> members
) {
	public static ChatMessageCreateEvent of(ChatMessage message, ChatRoom room, User sender) {
		return new ChatMessageCreateEvent(
			message.getId(),
			room.getId(),
			sender.getId(),
			sender.getUsername(),
			message.getContent(),
			message.getMessageType(),
			message.getMessageStatus(),
			LocalDateTime.ofInstant(message.getCreatedAt(), ZoneOffset.UTC),
			room.getChatMembers()
		);
	}
}
