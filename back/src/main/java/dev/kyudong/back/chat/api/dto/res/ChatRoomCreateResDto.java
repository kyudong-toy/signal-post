package dev.kyudong.back.chat.api.dto.res;

import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.RoomStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ChatRoomCreateResDto(
		Long roomId,
		int memberCount,
		RoomStatus status,
		LocalDateTime createdAt
) {
	public static ChatRoomCreateResDto from(ChatRoom chatRoom) {
		return new ChatRoomCreateResDto(
				chatRoom.getId(),
				chatRoom.getChatMembers().size(),
				chatRoom.getStatus(),
				LocalDateTime.ofInstant(chatRoom.getCreatedAt(), ZoneOffset.UTC)
		);
	}
}
