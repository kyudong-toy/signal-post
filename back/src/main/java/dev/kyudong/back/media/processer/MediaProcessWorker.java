package dev.kyudong.back.media.processer;

import dev.kyudong.back.media.api.dto.ImageProcessingResult;
import dev.kyudong.back.media.api.dto.MediaFileInfo;
import dev.kyudong.back.media.api.dto.VideoProcessingResult;
import dev.kyudong.back.media.api.dto.event.ImageProcessingDto;
import dev.kyudong.back.media.api.dto.event.ProcessingPayload;
import dev.kyudong.back.media.api.dto.event.VideoProcessingDto;
import dev.kyudong.back.media.domain.ImageMediaFile;
import dev.kyudong.back.media.domain.VideoMediaFile;
import dev.kyudong.back.media.exception.MediaFileMetadataNotFoundException;
import dev.kyudong.back.media.manager.MediaFileStorageManager;
import dev.kyudong.back.media.repository.MediaFileRepository;
import dev.kyudong.back.media.utils.FFmpegWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaProcessWorker {

	private final MediaFileStorageManager mediaFileStorageManager;
	private final MediaFileRepository mediaFileRepository;
	private final MediaProcessSender processSender;
	private final FFmpegWorker ffmpegWorker;

	@Transactional
	@RabbitListener(queues = "media.video.process.queue")
	public void handleVideoProcess(MediaFileInfo fileInfo) {
		final Long fileId = fileInfo.fileId();
		VideoMediaFile videoMediaFile = (VideoMediaFile) mediaFileRepository.findById(fileId)
				.orElseThrow(() -> {
					log.error("파일 정보를 찾을 수 없습니다: fileId={}", fileId);
					return new MediaFileMetadataNotFoundException(fileId);
				});

		try {
			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							VideoProcessingDto.processing(fileId)
					)
			);
			File originalFile = mediaFileStorageManager.reassemble(fileInfo.uploadId(), fileInfo.fileName());

			VideoProcessingResult result = ffmpegWorker.processVideo(originalFile);
			videoMediaFile.updateThumbnailPath(result.thumbnailPath());
			videoMediaFile.updateFilePath(result.outputPath());
			videoMediaFile.updateStoredFileName(result.storedFileName());
			videoMediaFile.pending();

			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							VideoProcessingDto.complete(
									fileId,
									videoMediaFile.getThumbnailPath()
							)
					)
			);
		} catch (Exception e) {
			log.debug("파일 정보: fileInfo={}", fileInfo);
			log.error("비디오 인코딩 중 에러가 발생했습니다", e);
			videoMediaFile.processingFail();
			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							VideoProcessingDto.failed(fileId)
					)
			);
		}
	}

	@Transactional
	@RabbitListener(queues = "media.image.process.queue")
	public void handleImageProcess(MediaFileInfo fileInfo) {
		final Long fileId = fileInfo.fileId();
		ImageMediaFile imageMediaFile = (ImageMediaFile) mediaFileRepository.findById(fileId)
				.orElseThrow(() -> {
					log.error("파일 정보를 찾을 수 없습니다: fileId={}", fileId);
					return new MediaFileMetadataNotFoundException(fileId);
				});

		try {
			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							ImageProcessingDto.processing(fileId)
					)
			);
			File originalFile = mediaFileStorageManager.reassemble(fileInfo.uploadId(), fileInfo.fileName());

			ImageProcessingResult result = ffmpegWorker.processImage(originalFile);
			imageMediaFile.updateFilePath(result.outputPath());
			imageMediaFile.updateStoredFileName(result.storedFileName());
			imageMediaFile.pending();

			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							ImageProcessingDto.complete(fileId)
					)
			);
		} catch (Exception e) {
			log.debug("파일 정보: fileInfo={}", fileInfo);
			log.error("비디오 인코딩 중 에러가 발생했습니다", e);
			imageMediaFile.processingFail();
			processSender.sendMediaFileProgress(
					ProcessingPayload.of(
							fileInfo.uploaderId(),
							ImageProcessingDto.failed(fileId)
					)
			);
		}
	}

}
