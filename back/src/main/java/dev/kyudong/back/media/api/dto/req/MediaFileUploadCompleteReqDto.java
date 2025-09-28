package dev.kyudong.back.media.api.dto.req;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MediaFileUploadCompleteReqDto(
		@NotNull
		UUID uploadId
) {
}
