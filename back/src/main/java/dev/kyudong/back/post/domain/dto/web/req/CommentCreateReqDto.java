package dev.kyudong.back.post.domain.dto.web.req;

import jakarta.validation.constraints.NotNull;

public record CommentCreateReqDto(
		@NotNull(message = "본문은 공백으로 올 수 없습니다.")
		Object content
) {
}
