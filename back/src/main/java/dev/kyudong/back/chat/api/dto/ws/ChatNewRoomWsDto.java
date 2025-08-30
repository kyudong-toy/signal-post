package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatRoomCreateEvent;
import dev.kyudong.back.chat.domain.ChatMember;

import java.time.LocalDateTime;

public record ChatNewRoomWsDto(
		Long roomId,
		String roomName,
		Long userId,
		String userName,
		LocalDateTime createdAt
) {
	public static ChatNewRoomWsDto of(ChatRoomCreateEvent event, ChatMember member) {
		return new ChatNewRoomWsDto(
				event.roomId(),
				member.getRoomName(),
				member.getUser().getId(),
				member.getUser().getUsername(),
				event.createdAt()
		);
	}
}
