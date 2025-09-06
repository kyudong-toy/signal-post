package dev.kyudong.back.post.domain.dto.web.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PostUpdateReqDto(
		@Size(max = 100, message = "제목은 100글자를 초과할 수 없습니다.")
		@NotBlank(message = "제목은 공백으로 올 수 없습니다.")
		String subject,

		@NotNull(message = "본문은 공백으로 올 수 없습니다.")
		Object content,

		@NotBlank(message = "카테고리가 선택되지 않았습니다")
		String categoryCode,

		@NotNull(message = "파일 아이디 목록은 NULL이 올 수 없습니다.")
		Set<@NotNull(message = "파일 아이디는 NULL이 들어올 수 없습니다.") @Positive Long> fileIds,

		@NotNull(message = "태그 목록이 비어있습니다")
		Set<String> tags
) {
}
