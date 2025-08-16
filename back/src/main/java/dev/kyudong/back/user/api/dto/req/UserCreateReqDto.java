package dev.kyudong.back.user.api.dto.req;

public record UserCreateReqDto(
		String userName,
		String passWord
) {
}
