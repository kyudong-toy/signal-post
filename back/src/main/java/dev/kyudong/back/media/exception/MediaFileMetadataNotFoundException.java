package dev.kyudong.back.media.exception;

public class MediaFileMetadataNotFoundException extends RuntimeException {
	public MediaFileMetadataNotFoundException(final Long fileId) {
		super(String.format("요청한 filedId: %s 의 정보를 찾지 못했습니다.", fileId));
	}
}
