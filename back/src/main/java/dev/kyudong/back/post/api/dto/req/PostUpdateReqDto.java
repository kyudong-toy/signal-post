package dev.kyudong.back.post.api.dto.req;

public record PostUpdateReqDto(
		long userId,
		String subject,
		String content
) {
}
