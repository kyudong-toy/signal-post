package dev.kyudong.back.post.domain.dto.web.req;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateReqDto(
		@NotBlank(message = "Content can not be blank.")
		String content
) {
}
