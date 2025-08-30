package dev.kyudong.back.chat.api.dto.event;

import dev.kyudong.back.chat.domain.ChatMember;

import java.util.Set;

public record ChatMemberLeftEvent(
		Long roomId,
		Long leaveUserId,
		Set<ChatMember> members
) {
	public static ChatMemberLeftEvent of(Long roomId, Long leaveUserId, Set<ChatMember> members) {
		return new ChatMemberLeftEvent(roomId, leaveUserId, members);
	}
}
