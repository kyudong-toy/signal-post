package dev.kyudong.back.file.utils;

import dev.kyudong.back.file.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Slf4j
public class FileValidationUtils {

	// MIME 타입과 허용 확장자 매핑, 일부 시스템에서 image/jpg로 가져오는 경우가 있음.
	private static final Map<String, Set<String>> MIME_TYPE_EXTENSIONS = Map.of(
			"image/jpeg", Set.of("jpg", "jpeg"),
			"image/jpg", Set.of("jpg", "jpeg"),
			"image/png", Set.of("png"),
			"image/gif", Set.of("gif"),
			"application/pdf", Set.of("pdf"),
			"video/mp4", Set.of("mp4"),
			"text/plain", Set.of("txt"),
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of("docx")
	);

	// 허용된 확장자 목록
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
			"jpg", "jpeg", "png", "gif", "pdf", "mp4", "txt", "docx"
	);

	/**
	 * 파일 확장자와 MIME 타입이 일치하는지 검증
	 */
	public static void validateFileTypeConsistency(String originalFileName, String mimeType, Long uploaderId) {
		if (originalFileName == null || mimeType == null) {
			log.error("파일명 또는 MIME 타입이 null입니다: uploaderId={}", uploaderId);
			throw new InvalidFileException("파일 정보가 올바르지 않습니다.");
		}

		String fileExtension = extractFileExtension(originalFileName);

		// 1. 허용된 확장자인지 확인
		if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
			log.error("허용되지 않은 확장자입니다: uploaderId={}, extension={}", uploaderId, fileExtension);
			throw new InvalidFileException("허용되지 않은 파일 형식입니다.");
		}

		// 2. MIME 타입이 허용된 타입인지 확인
		if (!MIME_TYPE_EXTENSIONS.containsKey(mimeType)) {
			log.error("허용되지 않은 MIME 타입입니다: uploaderId={}, mimeType={}", uploaderId, mimeType);
			throw new InvalidFileException("허용되지 않은 파일 형식입니다.");
		}

		// 3. 확장자와 MIME 타입이 일치하는지 확인
		Set<String> allowedExtensions = MIME_TYPE_EXTENSIONS.get(mimeType);
		if (!allowedExtensions.contains(fileExtension)) {
			log.error("파일 확장자와 MIME 타입이 일치하지 않습니다: uploaderId={}, fileName={}, mimeType={}",
					uploaderId, originalFileName, mimeType);
			throw new InvalidFileException("파일 확장자와 내용이 일치하지 않습니다.");
		}

		log.info("파일 타입 검증 통과완료: uploaderId={}, fileName={}, mimeType={}",
				uploaderId, originalFileName, mimeType);
	}

	/**
	 * 파일명에서 확장자 추출 (소문자로 변환)
	 */
	private static String extractFileExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			throw new InvalidFileException("파일 확장자가 없습니다.");
		}

		int lastDotIndex = fileName.lastIndexOf(".");
		if (lastDotIndex == fileName.length() - 1) {
			throw new InvalidFileException("파일 확장자가 올바르지 않습니다.");
		}

		return fileName.substring(lastDotIndex + 1).toLowerCase();
	}

	/**
	 * 파일명 안전성 검증 (추가 보안 검증)
	 */
	public static void validateFileName(String fileName) {
		if (!StringUtils.hasText(fileName)) {
			throw new InvalidFileException("파일명이 비어있습니다.");
		}

		// 위험한 문자들 검증
		String[] dangerousChars = {"../", "..\\", "<", ">", ":", "\"", "|", "?", "*"};
		for (String dangerousChar : dangerousChars) {
			if (fileName.contains(dangerousChar)) {
				throw new InvalidFileException("파일명에 허용되지 않은 문자가 포함되어 있습니다.");
			}
		}

		if (fileName.length() > 255) {
			throw new InvalidFileException("파일명이 너무 깁니다.");
		}
	}

}
