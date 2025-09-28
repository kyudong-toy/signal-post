package dev.kyudong.back.media.api.dto.req;

import dev.kyudong.back.media.domain.MediaFileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MediaFileUploadStartReqDto(
		@NotBlank
		String fileName,

		@NotBlank
		String mimeType,

		@Positive
		long fileSize,

		@Positive
		int totalChunkCount,

		@NotNull
		MediaFileType type
) {
}
