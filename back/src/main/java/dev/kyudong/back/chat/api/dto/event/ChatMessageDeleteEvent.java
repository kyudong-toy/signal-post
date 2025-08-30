package dev.kyudong.back.chat.api.dto.event;

import dev.kyudong.back.chat.domain.*;

import java.util.Set;

public record ChatMessageDeleteEvent(
		Long roomId,
		Long messageId,
		Long userId,
		MessageType messageType,
		MessageStatus messageStatus,
		Set<ChatMember> members
) {
	public static ChatMessageDeleteEvent of(ChatMessage message, ChatRoom room, Long userId) {
		return new ChatMessageDeleteEvent(
			message.getChatRoom().getId(),
			message.getId(),
			userId,
			message.getMessageType(),
			message.getMessageStatus(),
			room.getChatMembers()
		);
	}
}
