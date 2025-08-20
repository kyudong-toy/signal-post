package dev.kyudong.back.file.manager;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageManager {

	/**
	 * 파일을 지정된 이름으로 저장하고, 저장된 전체 경로를 반환합니다.
	 * @param multipartFile 저장할 파일
	 * @param storedFileName 서버에 저장될 고유한 파일 이름
	 * @return 저장된 파일의 전체 접근 경로 (예: /uploads/uuid-abc.png)
	 */
	String store(MultipartFile multipartFile, String storedFileName) throws IOException;

	/**
	 * 지정된 이름의 파일을 삭제합니다.
	 * @param storedFileName 서버에 저장될 고유한 파일 이름
	 */
	void delete(String storedFileName) throws IOException;

}
