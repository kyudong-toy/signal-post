package dev.kyudong.back.media.service;

import dev.kyudong.back.media.api.dto.MediaFileInfo;
import dev.kyudong.back.media.api.dto.MediaFileTransaction;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadCompleteReqDto;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadResDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadStartResDto;
import dev.kyudong.back.media.domain.*;
import dev.kyudong.back.media.exception.InvalidMediaFileException;
import dev.kyudong.back.media.manager.MediaFileStorageManager;
import dev.kyudong.back.media.repository.MediaFileRepository;
import dev.kyudong.back.media.utils.MediaFileValidationUtils;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileService {

	private final MediaFileRepository mediaFileRepository;
	private final RedissonClient redissonClient;
	private final MediaFileStorageManager mediaFileStorageManager;
	private final UserReaderService userReaderService;
	private final RabbitTemplate rabbitTemplate;

	/**
	 * 파일 업로드를 위한 사전 작업으로 임시 아이디 발급과 진행상황을 저장합니다
	 *
	 * @param uploaderId  파일 등록자
	 * @param request	  파일 정보
	 * @return 임시 발급 아이디
	 */
	public MediaFileUploadStartResDto uploadStart(Long uploaderId, MediaFileUploadStartReqDto request) {
		String originalFileName = request.fileName();
		String mimeType = request.mimeType();
		MediaFileValidationUtils.validateFileTypeConsistency(originalFileName, mimeType, uploaderId);

		String uploadId = UUID.randomUUID().toString();
		MediaFileTransaction transaction = MediaFileTransaction.of(uploaderId, request);
		RBucket<MediaFileTransaction> bucket = redissonClient.getBucket("uploads:" + uploadId);
		bucket.set(transaction, Duration.ofHours(1));

		return new MediaFileUploadStartResDto(uploadId);
	}

	/**
	 * 개별 파일을 저장하고 진행상황을 갱신합니다
	 *
	 * @param uploaderId	파일 등록자
	 * @param chunk			파일 조각
	 * @param uploadId		임시 발급 아이디
	 * @param chunkNumber	파일 조각 넘버
	 */
	public void saveChunk(Long uploaderId, MultipartFile chunk, UUID uploadId, int chunkNumber) {
		RBucket<MediaFileTransaction> bucket = redissonClient.getBucket("uploads:" + uploadId);
		MediaFileTransaction transaction = bucket.get();

		if (transaction == null) {
			throw new InvalidMediaFileException("업로드 중인 파일이 아닙니다");
		}

		if (!transaction.getUploaderId().equals(uploaderId)) {
			throw new AccessDeniedException("파일 업로드에 대한 권한이 없습니다");
		}

		if (transaction.hasChunk(chunkNumber)) {
			log.debug("중복된 청크 수신으로 무시됩니다: uploaderId={}, chunkNumber={}", uploaderId, chunkNumber);
			return;
		}

		// 파일 저장
		mediaFileStorageManager.store(chunk, uploadId, chunkNumber);

		// 갱신
		transaction.addChunk(chunkNumber);
		bucket.set(transaction, Duration.ofHours(1));
	}

	/**
	 * 개별 파일을 하나의 파일로 합치고 타입에 따라 추가작업을 진행합니다
	 *
	 * @param uploaderId  파일 등록자
	 * @param request     업로드 파일 검증용 객체
	 * @return 결과값
	 */
	@Transactional
	public MediaFileUploadResDto completeUpload(Long uploaderId, MediaFileUploadCompleteReqDto request) {
		RBucket<MediaFileTransaction> bucket = redissonClient.getBucket("uploads:" + request.uploadId());
		MediaFileTransaction transaction = bucket.get();

		if (transaction == null) {
			throw new InvalidMediaFileException("업로드 중인 파일이 아닙니다");
		}

		if (!transaction.isComplete()) {
			log.error("파일 업로드가 완료되지 않았습니다: uploadId={}, transaction={}", request.uploadId(), transaction);
			throw new InvalidMediaFileException("파일 업로드가 완료되지 않았습니다");
		}

		User uploader = userReaderService.getUserReference(uploaderId);
		MediaFileType type = transaction.getType();
		MediaFile newMediaFile = switch (type) {
			case IMAGE -> ImageMediaFile.create(
					uploader,
					transaction.getFileName(),
					transaction.getFileSize(),
					transaction.getMimeType()
			);
			case VIDEO -> VideoMediaFile.create(
					uploader,
					transaction.getFileName(),
					transaction.getFileSize(),
					transaction.getMimeType()
			);
			default -> throw new IllegalArgumentException("지원하지 않는 미디어 타입입니다: " + transaction.getType());
		};
		MediaFile savedMediaFile = mediaFileRepository.save(newMediaFile);

		String routingKey = switch (transaction.getType()) {
			case IMAGE -> "media.image.process";
			case VIDEO -> "media.video.process";
			default -> throw new IllegalArgumentException("지원하지 않는 미디어 타입입니다: " + transaction.getType());
		};

		MediaFileInfo fileInfo = MediaFileInfo.of(
				uploaderId,
				savedMediaFile.getId(),
				savedMediaFile.getOriginalFileName(),
				request.uploadId()
		);
		rabbitTemplate.convertAndSend("media.exchange", routingKey, fileInfo);

		// 청크 이력을 삭제한다.
		bucket.delete();

		return MediaFileUploadResDto.from(savedMediaFile);
	}

}

