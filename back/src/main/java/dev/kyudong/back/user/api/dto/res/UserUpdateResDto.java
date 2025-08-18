package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserUpdateResDto(
		Long id,
		String username,
		UserStatus status
) {
	public static UserUpdateResDto from(User user) {
		return new UserUpdateResDto(
				user.getId(), user.getUsername(), user.getStatus()
		);
	}
}
