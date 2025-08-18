package dev.kyudong.back.post.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommentUpdateReqDto(
		@NotNull(message = "UserId can not be null.")
		@Positive(message = "UserId must be positive.")
		Long userId,

		@NotBlank(message = "Content can not be blank.")
		String content
) {
}
