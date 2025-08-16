package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserUpdateResDto(
		long id,
		String userName,
		UserStatus status
) {
	public static UserUpdateResDto form(User user) {
		return new UserUpdateResDto(
				user.getId(), user.getUserName(), user.getStatus()
		);
	}
}
