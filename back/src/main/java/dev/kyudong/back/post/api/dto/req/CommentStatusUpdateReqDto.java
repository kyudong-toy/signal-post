package dev.kyudong.back.post.api.dto.req;

import dev.kyudong.back.post.domain.CommentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommentStatusUpdateReqDto(
		@NotNull(message = "UserId can not be null.")
		@Positive(message = "UserId must be positive.")
		Long userId,

		@NotNull(message = "CommentStatus can not be null.")
		CommentStatus status
) {
}
