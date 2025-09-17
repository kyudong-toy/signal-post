package dev.kyudong.back.user.api.dto;

import dev.kyudong.back.user.api.dto.res.UserValidateResDto;

public record UserReissueDto(
		UserValidateResDto response,
		String refreshToken
) {
	public static UserReissueDto from(UserValidateResDto response, String refreshToken) {
		return new UserReissueDto(response, refreshToken);
	}
}
