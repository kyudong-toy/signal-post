package dev.kyudong.back.chat.api.dto.event;

import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatRoom;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

public record ChatMemberInviteEvent(
		Long roomId,
		Set<Long> existsUserIds,
		Set<ChatMember> newMembers,
		LocalDateTime lastActivityAt
) {
	public static ChatMemberInviteEvent of(ChatRoom chatRoom, Set<Long> existsUserIds, Set<ChatMember> newMembers) {
		return new ChatMemberInviteEvent(
				chatRoom.getId(),
				existsUserIds,
				newMembers,
				LocalDateTime.ofInstant(chatRoom.getLastActivityAt(), ZoneOffset.UTC)
		);
	}
}
