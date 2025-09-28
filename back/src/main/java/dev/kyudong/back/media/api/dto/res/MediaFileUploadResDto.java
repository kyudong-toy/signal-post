package dev.kyudong.back.media.api.dto.res;

import dev.kyudong.back.media.domain.MediaFile;

public record MediaFileUploadResDto(
		Long id
) {
	public static MediaFileUploadResDto from(MediaFile mediaFile) {
		return new MediaFileUploadResDto(mediaFile.getId());
	}
}
