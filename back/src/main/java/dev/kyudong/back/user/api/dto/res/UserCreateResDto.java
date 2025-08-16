package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;

public record UserCreateResDto(
		long id,
		String userName
) {
	public static UserCreateResDto from(User user) {
		return new UserCreateResDto(
				user.getId(),
				user.getUserName()
		);
	}
}
