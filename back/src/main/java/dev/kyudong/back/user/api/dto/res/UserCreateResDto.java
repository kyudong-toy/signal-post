package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;

public record UserCreateResDto(
		Long id,
		String username,
		UserStatus status,
		UserRole role
) {
	public static UserCreateResDto from(User user) {
		return new UserCreateResDto(
				user.getId(), user.getUsername(), user.getStatus(), user.getRole()
		);
	}
}
