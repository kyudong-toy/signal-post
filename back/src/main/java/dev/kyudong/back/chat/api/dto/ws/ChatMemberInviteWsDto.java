package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatMemberInviteEvent;
import dev.kyudong.back.chat.domain.ChatMember;

import java.time.LocalDateTime;

public record ChatMemberInviteWsDto(
		Long roomId,
		String roomName,
		LocalDateTime createdAt
) {
	public static ChatMemberInviteWsDto of(ChatMemberInviteEvent event, ChatMember member) {
		return new ChatMemberInviteWsDto(
				event.roomId(),
				member.getRoomName(),
				event.lastActivityAt()
		);
	}
}
