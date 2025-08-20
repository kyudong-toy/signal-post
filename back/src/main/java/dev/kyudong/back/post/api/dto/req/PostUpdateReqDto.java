package dev.kyudong.back.post.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PostUpdateReqDto(
		@Size(max = 100, message = "제목은 100글자를 초과할 수 없습니다.")
		@NotBlank(message = "제목은 공백으로 올 수 없습니다.")
		String subject,

		@NotBlank(message = "본문 내용은 공백으로 올 수 없습니다.")
		String content,

		@NotNull(message = "파일 아이디 목록은 NULL이 올 수 없습니다.")
		Set<@NotNull(message = "파일 아이디는 NULL이 들어올 수 없습니다.") @Positive Long> fileIds,

		@NotNull(message = "삭제한 파일 아이디 목록은 NULL이 올 수 없습니다.")
		Set<@NotNull(message = "삭제한 파일 아이디는 NULL이 들어올 수 없습니다.") @Positive Long> delFileIds
) {
}
