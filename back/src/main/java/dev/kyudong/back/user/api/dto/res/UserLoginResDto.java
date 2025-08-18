package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;

public record UserLoginResDto(
		Long id,
		String username,
		String token
) {
	public static UserLoginResDto from(User user, String token) {
		return new UserLoginResDto(
				user.getId(), user.getUsername(), token
		);
	}
}
