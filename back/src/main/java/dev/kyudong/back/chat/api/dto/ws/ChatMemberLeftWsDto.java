package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatMemberLeftEvent;

public record ChatMemberLeftWsDto(
		Long roomId,
		Long leaveUserId
) {
	public static ChatMemberLeftWsDto from(ChatMemberLeftEvent event) {
		return new ChatMemberLeftWsDto(event.roomId(), event.leaveUserId());
	}
}
