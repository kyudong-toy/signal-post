package dev.kyudong.back.post.domain.dto.web.req;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateReqDto(
		@NotBlank(message = "Content can not be blank.")
		String content
) {
}
