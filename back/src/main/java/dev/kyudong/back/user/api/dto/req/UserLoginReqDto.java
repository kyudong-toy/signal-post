package dev.kyudong.back.user.api.dto.req;

public record UserLoginReqDto(
		String userName,
		String passWord
) {
}
