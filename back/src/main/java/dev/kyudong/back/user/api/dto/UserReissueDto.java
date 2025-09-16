package dev.kyudong.back.user.api.dto;

import dev.kyudong.back.user.api.dto.res.UserReissueResDto;

public record UserReissueDto(
		UserReissueResDto response,
		String refreshToken
) {
	public static UserReissueDto from(UserReissueResDto response, String refreshToken) {
		return new UserReissueDto(response, refreshToken);
	}
}
