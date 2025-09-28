package dev.kyudong.back.media.exception;

public class MediaFileStoredException extends RuntimeException {
	public MediaFileStoredException() {
		super("파일 업로드에 실패했습니다");
	}
}
