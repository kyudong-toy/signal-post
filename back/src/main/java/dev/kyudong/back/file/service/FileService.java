package dev.kyudong.back.file.service;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileOwnerType;
import dev.kyudong.back.file.exception.FileMetadataNotFoundException;
import dev.kyudong.back.file.exception.InvalidFileException;
import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.properties.FileStorageProperties;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.file.utils.FileValidationUtils;
import dev.kyudong.back.post.api.dto.event.PostCreatedEvent;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

	private final FileRepository fileRepository;
	private final UserRepository userRepository;
	private final FileStorageManager fileStorageManager;
	private final FileStorageProperties fileStorageProperties;

	@Transactional
	public FileUploadResDto storeTempFile(final Long uploaderId, final MultipartFile multipartFile) throws IOException {
		if (multipartFile.isEmpty()) {
			log.error("업로드 요청 파일의 데이터가 누락되었습니다: fileData={}", multipartFile);
			throw new InvalidFileException("Invalid Request File");
		}

		String originalFileName = multipartFile.getOriginalFilename();
		String mimeType = multipartFile.getContentType();
		long fileSize=  multipartFile.getSize();
		FileValidationUtils.validateFileTypeConsistency(originalFileName, mimeType, uploaderId);

		String storedFileName = UUID.randomUUID() + "." + originalFileName;
		FileValidationUtils.validateFileName(storedFileName);

		if (!userRepository.existsById(uploaderId)) {
			log.warn("사용자 조회에 실패했습니다: uploaderId={}", uploaderId);
			throw new UserNotFoundException(uploaderId);
		}
		User uploader = userRepository.getReferenceById(uploaderId);

		String filePath = null;
		try {
			log.debug("파일 업로드 시작합니다: uploaderId: {}, originalFileName: {}, mimeType: {}", uploaderId, originalFileName, mimeType);

			// 파일 업로드.
			filePath = fileStorageManager.store(multipartFile, storedFileName);

			// 파일 접근 주소
			String webPath = fileStorageProperties.getPublicWebPath() + storedFileName;
			File newFile = File.builder()
					.uploader(uploader)
					.originalFileName(originalFileName)
					.storedFileName(storedFileName)
					.mimeType(mimeType)
					.filePath(filePath)
					.fileSize(fileSize)
					.webPath(webPath)
					.build();

			// 파일 메타데이터 저장.
			File savedFile = fileRepository.save(newFile);
			log.info("파일 업로드 완료: uploaderId={}, id={}", uploaderId, savedFile.getId());

			return FileUploadResDto.from(savedFile);
		} catch (Exception e) {
			log.error("파일 업로드 중 오류 발생으로 파일을 롤백합니다: storedFileName={}", storedFileName);

			if (StringUtils.hasText(filePath)) {
				fileStorageManager.delete(storedFileName);
			}

			throw new RuntimeException("파일 업로드에 실패했습니다.", e);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handlePostCreatedEvent(PostCreatedEvent event) {
		log.info("게시글 생성 이벤트 수신완료: userId={}, postId={}", event.userId(), event.postId());

		if (event.fileIds().isEmpty()) {
			log.debug("게시글({})에 첨부파일이 없어 생략합니다.", event.postId());
			return;
		}

		Long uploaderId = event.userId();
		if (!userRepository.existsById(uploaderId)) {
			log.warn("사용자 조회에 실패했습니다: uploaderId={}", uploaderId);
			throw new UserNotFoundException(uploaderId);
		}
		User uploader = userRepository.getReferenceById(uploaderId);

		for (Long fileId : event.fileIds()) {
			File file = fileRepository.findByIdAndUploader(fileId, uploader)
					.orElseThrow(() -> {
						log.error("파일 정보 불일치로 파일 상태 업데이트가 취소됩니다: event={}", event);
						return new FileMetadataNotFoundException(fileId);
					});
			file.activeFile();
			file.updateOwnerId(event.postId());
			file.updateFileOwnerType(FileOwnerType.POST);
		}

	}

}

