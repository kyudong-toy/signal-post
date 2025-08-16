package dev.kyudong.back.user.api.dto.req;

import dev.kyudong.back.user.domain.UserStatus;

public record UserStatusUpdateReqDto(
		String passWord,
		UserStatus userStatus
) {
}
