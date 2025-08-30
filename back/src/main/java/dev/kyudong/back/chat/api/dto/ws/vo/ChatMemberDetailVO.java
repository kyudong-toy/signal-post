package dev.kyudong.back.chat.api.dto.ws.vo;

import dev.kyudong.back.user.domain.User;

public record ChatMemberDetailVO(
		Long userId,
		String userName
) {
	public static ChatMemberDetailVO from(User user) {
		return new ChatMemberDetailVO(
				user.getId(),
				user.getUsername()
		);
	}
}
