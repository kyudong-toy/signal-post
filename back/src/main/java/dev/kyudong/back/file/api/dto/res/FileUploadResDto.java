package dev.kyudong.back.file.api.dto.res;

import dev.kyudong.back.file.domain.File;

public record FileUploadResDto(
	Long id,
	String webPath
) {
	public static FileUploadResDto from(File file) {
		return new FileUploadResDto(
				file.getId(), file.getWebPath()
		);
	}
}
