package dev.kyudong.back.file.exception;

public class FileMetadataNotFoundException extends RuntimeException {
	public FileMetadataNotFoundException(final Long fileId) {
		super(String.format("요청한 filedId: %s 의 정보를 찾지 못했습니다.", fileId));
	}
}
