package dev.kyudong.back.post.api.dto.req;

public record PostCreateReqDto(
		long userId,
		String subject,
		String content
) {
}
