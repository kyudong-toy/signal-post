package dev.kyudong.back.media.api.dto;

public record ImageProcessingResult(
		String storedFileName,
		String outputPath
) {
}
