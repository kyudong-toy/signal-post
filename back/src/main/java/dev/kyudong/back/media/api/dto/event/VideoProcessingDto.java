package dev.kyudong.back.media.api.dto.event;

/**
 * 비디오 인코딩시 클라이언트에게 상태 전송을 위한 객체입니다.
 * @param status			현재 상태
 * @param fileId			파일 아이디
 * @param thumbnailPath		썸네일 경로
 */
public record VideoProcessingDto(
		String status,
		Long fileId,
		String thumbnailPath
) implements ProcessingDto {
	public static VideoProcessingDto processing(Long fileId) {
		return new VideoProcessingDto("PROCESSING", fileId, null);
	}

	public static VideoProcessingDto complete(Long fileId, String thumbnailPath) {
		return new VideoProcessingDto("COMPLETE", fileId, thumbnailPath);
	}

	public static VideoProcessingDto failed(Long fileId) {
		return new VideoProcessingDto("FAILED", fileId, null);
	}

}