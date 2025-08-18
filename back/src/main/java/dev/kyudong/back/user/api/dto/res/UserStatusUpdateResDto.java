package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserStatusUpdateResDto(
		Long id,
		UserStatus status
) {
	public static UserStatusUpdateResDto from(User user) {
		return new UserStatusUpdateResDto(
				user.getId(), user.getStatus()
		);
	}
}
