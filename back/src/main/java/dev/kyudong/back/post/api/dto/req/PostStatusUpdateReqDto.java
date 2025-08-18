package dev.kyudong.back.post.api.dto.req;

import dev.kyudong.back.post.domain.PostStatus;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateReqDto(
		@NotNull(message = "PostStatus can not be null.")
		PostStatus status
) {
}
