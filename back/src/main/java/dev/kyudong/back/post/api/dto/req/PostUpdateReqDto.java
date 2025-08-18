package dev.kyudong.back.post.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateReqDto(
		@Size(max = 100, message = "Subject cannot be longer than 100 characters.")
		@NotBlank(message = "Subject can not be blank.")
		String subject,

		@NotBlank(message = "Content can not be blank.")
		String content
) {
}
