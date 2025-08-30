package dev.kyudong.back.chat.api.dto.event;

import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatRoom;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

public record ChatRoomCreateEvent(
		Long roomId,
		LocalDateTime createdAt,
		Set<ChatMember> chatMembers
) {
	public static ChatRoomCreateEvent from(ChatRoom chatRoom) {
		return new ChatRoomCreateEvent(
				chatRoom.getId(),
				LocalDateTime.ofInstant(chatRoom.getCreatedAt(), ZoneOffset.UTC),
				chatRoom.getChatMembers()
		);
	}
}
