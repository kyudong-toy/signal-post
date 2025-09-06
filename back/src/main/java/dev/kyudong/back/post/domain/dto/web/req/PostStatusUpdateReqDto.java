package dev.kyudong.back.post.domain.dto.web.req;

import dev.kyudong.back.post.domain.entity.PostStatus;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateReqDto(
		@NotNull(message = "PostStatus can not be null.")
		PostStatus status
) {
}
