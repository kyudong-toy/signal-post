package dev.kyudong.back.chat.api.dto.res;

import dev.kyudong.back.chat.domain.ChatRoom;

public record ChatRoomDetailResDto(
		Long roomId,
		Integer memberCount
) {
	public static ChatRoomDetailResDto from(ChatRoom chatRoom) {
		return new ChatRoomDetailResDto(
				chatRoom.getId(),
				chatRoom.getChatMembers().size()
		);
	}
}
