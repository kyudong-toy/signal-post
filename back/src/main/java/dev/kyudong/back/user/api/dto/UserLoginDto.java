package dev.kyudong.back.user.api.dto;

import dev.kyudong.back.user.api.dto.res.UserLoginResDto;

public record UserLoginDto(
		UserLoginResDto response,
		String refreshToken
) {
	public static UserLoginDto from(UserLoginResDto response, String refreshToken) {
		return new UserLoginDto(response, refreshToken);
	}
}
