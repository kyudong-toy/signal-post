package dev.kyudong.back.media.api.dto;

import java.util.UUID;

/**
 * 파일 저장시스템에서 사용됩니다.
 *
 * @param uploaderId 	파일 등록자
 * @param fileId		파일 아이디
 * @param fileName		파일 이름
 * @param uploadId		임시 발급 아이디
 */
public record MediaFileInfo(
		Long uploaderId,
		Long fileId,
		String fileName,
		UUID uploadId
) {
	public static MediaFileInfo of(Long uploaderId, Long fileId, String fileName, UUID tempId) {
		return new MediaFileInfo(uploaderId, fileId, fileName, tempId);
	}
}
