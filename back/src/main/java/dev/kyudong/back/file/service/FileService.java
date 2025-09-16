package dev.kyudong.back.file.service;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.exception.FileMetadataNotFoundException;
import dev.kyudong.back.file.exception.InvalidFileException;
import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.properties.FileStorageProperties;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.file.utils.FileValidationUtils;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

	private final FileRepository fileRepository;
	private final FileStorageManager fileStorageManager;
	private final FileStorageProperties fileStorageProperties;
	private final UserReaderService userReaderService;

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

		User uploader = userReaderService.getUserReference(uploaderId);

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
			log.debug("파일 업로드 완료: uploaderId={}, id={}", uploaderId, savedFile.getId());

			return FileUploadResDto.from(savedFile);
		} catch (Exception e) {
			log.error("파일 업로드 중 오류 발생으로 파일을 롤백합니다: storedFileName={}", storedFileName);

			if (StringUtils.hasText(filePath)) {
				fileStorageManager.delete(storedFileName);
			}

			throw new RuntimeException("파일 업로드에 실패했습니다.", e);
		}
	}

}

