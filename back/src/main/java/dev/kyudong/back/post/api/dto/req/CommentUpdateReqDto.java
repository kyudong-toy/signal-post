package dev.kyudong.back.post.api.dto.req;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateReqDto(
		@NotBlank(message = "Content can not be blank.")
		String content
) {
}
