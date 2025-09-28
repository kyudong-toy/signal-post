package dev.kyudong.back.media.api.dto.event;

/**
 * 이미지 파일 작업시 클라이언트에게 상태 전송을 위한 객체입니다.
 * @param status			현재 상태
 * @param fileId			파일 아이디
 */
public record ImageProcessingDto(
		String status,
		Long fileId
) implements ProcessingDto {
	public static ImageProcessingDto processing(Long fileId) {
		return new ImageProcessingDto("PROCESSING", fileId);
	}

	public static ImageProcessingDto complete(Long fileId) {
		return new ImageProcessingDto("COMPLETE", fileId);
	}

	public static ImageProcessingDto failed(Long fileId) {
		return new ImageProcessingDto("FAILED", fileId);
	}

}