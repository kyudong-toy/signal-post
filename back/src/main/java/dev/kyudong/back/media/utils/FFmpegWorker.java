package dev.kyudong.back.media.utils;

import dev.kyudong.back.media.api.dto.ImageProcessingResult;
import dev.kyudong.back.media.api.dto.VideoProcessingResult;
import dev.kyudong.back.media.properties.MediaFileStorageProperties;
import jodd.io.StreamGobbler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FFmpegWorker {

	private final MediaFileStorageProperties mediaFileStorageProperties;

	public VideoProcessingResult processVideo(File originalFile) {
		String baseName = UUID.randomUUID().toString();
		String originalFileName = originalFile.getName();

		Path hostPath = Paths.get(mediaFileStorageProperties.basePath()).normalize();
		Path processedDir = hostPath.resolve(mediaFileStorageProperties.subPaths().processDir());

		String storedFileName = baseName + "_" + originalFileName;
		String intputPath = convertToContainerPath(originalFile.getAbsolutePath());
		Path outputPath = processedDir.resolve(storedFileName);
		Path thumbnailPath = processedDir.resolve(storedFileName + "_thumb.webp");

		compressVideoInternal(intputPath, outputPath.toString());
		extractThumbnailInternal(outputPath.toString(), thumbnailPath.toString());

		return new VideoProcessingResult(storedFileName, outputPath.toString(), thumbnailPath.toString());
	}

	private void compressVideoInternal(String inputPath, String outputPath) {
		inputPath = inputPath.replace("\\", "/");
		outputPath = outputPath.replace("\\", "/");

		ProcessBuilder builder = new ProcessBuilder(
				"docker", "exec", "signal-ffmpeg", "ffmpeg",
				"-y",
				"-i", inputPath,
				"-c:v", "libx264",
				"-crf", "25",
				"-preset", "fast",
				outputPath
		);
		log.debug("FFmpeg 비디오 압축 명령어: {}", builder.command());

		try {
			excuteProcess(builder);
			log.debug("FFmpeg 비디오 압축 완료: outputPath={}", outputPath);
		} catch (Exception e) {
			log.error("FFmpeg 비디오 압축 중 에러가 발생하여 정지합니다", e);
			throw new RuntimeException("FFmpeg 압축 실패");
		}
	}

	private void extractThumbnailInternal(String inputPath, String thumbnailPath) {
		inputPath = inputPath.replace("\\", "/");
		thumbnailPath = thumbnailPath.replace("\\", "/");

		ProcessBuilder builder = new ProcessBuilder(
				"docker", "exec", "signal-ffmpeg", "ffmpeg",
				"-y",
				"-i", inputPath,
				"-ss", "00:00:01",
				"-vframes", "1",
				"-vf", "scale=320:-1",
				"-c:v", "libwebp",
				"-quality", "80",
				thumbnailPath
		);
		log.debug("FFmpeg 썸네일 명령어: {}", builder.command());

		try {
			excuteProcess(builder);
			log.debug("FFmpeg 썸네일 생성 완료: outputPath={}", thumbnailPath);
		} catch (Exception e) {
			log.error("FFmpeg 썸네일 추출 중 에러가 발생하여 정지합니다", e);
			throw new RuntimeException("FFmpeg 썸네일 추출 실패");
		}
	}

	public ImageProcessingResult processImage(File originalFile) {
		String baseName = UUID.randomUUID().toString();
		String originalFileNameWithoutExt = originalFile.getName().replaceFirst("[.][^.]+$", "");

		Path hostPath = Paths.get(mediaFileStorageProperties.basePath()).normalize();
		Path processedDir = hostPath.resolve(mediaFileStorageProperties.subPaths().processDir());

		String storedFileName = baseName + "_" + originalFileNameWithoutExt + ".webp";

		String intputPath = convertToContainerPath(originalFile.getAbsolutePath());
		Path outputPath = processedDir.resolve(storedFileName);
		convertImageToWebp(intputPath, outputPath.toString());

		return new ImageProcessingResult(storedFileName, outputPath.toString());
	}

	private void convertImageToWebp(String inputPath, String outputPath) {
		inputPath = inputPath.replace("\\", "/");
		outputPath = outputPath.replace("\\", "/");

		ProcessBuilder builder = new ProcessBuilder(
				"docker", "exec", "signal-ffmpeg", "ffmpeg",
				"-y",
				"-i", inputPath,
				"-c:v", "libwebp",
				"-quality", "80",
				outputPath
		);
		log.debug("FFmpeg webp 변환 명령어: {}", builder.command());

		try {
			excuteProcess(builder);
			log.debug("FFmpeg webp 변환 완료: outputPath={}", outputPath);
		} catch (Exception e) {
			log.error("FFmpeg webp 변환중 에러가 발생하여 정지합니다", e);
			throw new RuntimeException("FFmpeg webp 변환 실패");
		}
	}

	private String convertToContainerPath(String hostPath) {
		return hostPath.replace(mediaFileStorageProperties.hostPath(), mediaFileStorageProperties.basePath()).replace("\\", "/");
	}

	 private void excuteProcess(ProcessBuilder builder) throws Exception {
		Process process = builder.start();
		if (log.isDebugEnabled()) {
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err, "FFMPEG-ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
			errorGobbler.start();
			outputGobbler.start();
		}

		 int exitCode = process.waitFor();
		 if (exitCode != 0) {
			 throw new RuntimeException("FFmpeg Exit Code: " + exitCode);
		 }
	}

}
