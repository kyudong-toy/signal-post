package dev.kyudong.back.media.api.dto;

public record VideoProcessingResult(
		String storedFileName,
		String outputPath,
		String thumbnailPath
) {
}
