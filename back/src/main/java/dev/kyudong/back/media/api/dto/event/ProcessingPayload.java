package dev.kyudong.back.media.api.dto.event;

/**
 * 클라이언트에게 상태 전송을 위한 객체입니다.
 */
public record ProcessingPayload (
		String uploaderId,
		ProcessingDto payload
) {
	public static ProcessingPayload of(Long uploaderId, ProcessingDto payload) {
		return new ProcessingPayload(String.valueOf(uploaderId), payload);
	}
}