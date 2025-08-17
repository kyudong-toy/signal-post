package dev.kyudong.back.post.api.dto.req;

import dev.kyudong.back.post.domain.PostStatus;

public record PostStatusUpdateReqDto(
		long userId,
		PostStatus status
) {
}
