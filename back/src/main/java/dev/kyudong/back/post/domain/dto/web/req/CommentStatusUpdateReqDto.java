package dev.kyudong.back.post.domain.dto.web.req;

import dev.kyudong.back.post.domain.entity.CommentStatus;
import jakarta.validation.constraints.NotNull;

public record CommentStatusUpdateReqDto(
		@NotNull(message = "CommentStatus can not be null.")
		CommentStatus status
) {
}
