package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserPasswordUpdateResDto(
		Long id,
		String username,
		UserStatus status
) {
	public static UserPasswordUpdateResDto from(User user) {
		return new UserPasswordUpdateResDto(
				user.getId(), user.getUsername(), user.getStatus()
		);
	}
}
