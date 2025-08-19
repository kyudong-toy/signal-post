package dev.kyudong.back.post.api.dto.req;

import dev.kyudong.back.post.domain.CommentStatus;
import jakarta.validation.constraints.NotNull;

public record CommentStatusUpdateReqDto(
		@NotNull(message = "CommentStatus can not be null.")
		CommentStatus status
) {
}
