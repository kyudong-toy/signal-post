package dev.kyudong.back.user.api.dto.res;

public record UserReissueResDto(
		String token
) {
	public static UserReissueResDto from(String accessToken) {
		return new UserReissueResDto(accessToken);
	}
}
