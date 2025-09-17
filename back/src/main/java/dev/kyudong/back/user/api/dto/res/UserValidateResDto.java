package dev.kyudong.back.user.api.dto.res;

public record UserValidateResDto(
		String token
) {
	public static UserValidateResDto from(String token) {
		return new UserValidateResDto(token);
	}
}
