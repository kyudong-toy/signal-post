package dev.kyudong.back.user.api.dto;

import dev.kyudong.back.user.api.dto.res.UserValidateResDto;

public record UserLoginDto(
		UserValidateResDto response,
		String refreshToken
) {
	public static UserLoginDto from(UserValidateResDto response, String refreshToken) {
		return new UserLoginDto(response, refreshToken);
	}
}
