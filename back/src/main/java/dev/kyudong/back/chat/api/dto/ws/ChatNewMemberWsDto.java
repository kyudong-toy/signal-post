package dev.kyudong.back.chat.api.dto.ws;

import dev.kyudong.back.chat.api.dto.event.ChatMemberInviteEvent;
import dev.kyudong.back.chat.api.dto.ws.vo.ChatMemberDetailVO;
import dev.kyudong.back.chat.domain.ChatMember;

import java.time.LocalDateTime;
import java.util.List;

public record ChatNewMemberWsDto(
		Long roomId,
		List<ChatMemberDetailVO> memberList,
		LocalDateTime lastActivityAt
) {
	public static ChatNewMemberWsDto from(ChatMemberInviteEvent event) {
		List<ChatMemberDetailVO> memberList = event.newMembers().stream()
																.map(ChatMember::getUser)
																.map(ChatMemberDetailVO::from)
																.toList();
		return new ChatNewMemberWsDto(event.roomId(), memberList, event.lastActivityAt());
	}
}
