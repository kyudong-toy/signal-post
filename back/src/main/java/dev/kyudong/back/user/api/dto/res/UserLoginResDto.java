package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserLoginResDto(
		long id,
		String userName,
		UserStatus status
) {
	public static UserLoginResDto from(User user) {
		return new UserLoginResDto(
				user.getId(), user.getUserName(), user.getStatus()
		);
	}
}
