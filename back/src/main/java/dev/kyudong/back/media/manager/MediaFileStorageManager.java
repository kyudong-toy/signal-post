package dev.kyudong.back.media.manager;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface MediaFileStorageManager {

	/**
	 * 파일 조각을 디스크에 저장합니다
	 * @param file			파일 조각
	 * @param uploadId		임시 발급 아이디
	 * @param chunkNumber	파일 조각 넘버
	 */
	void store(MultipartFile file, UUID uploadId, int chunkNumber);

	/**
	 * 파일 조각을 하나의 파일로 완성합니다
	 * @param uploadId		임시 발급 아이디
	 * @param fileName		파일 이름
	 */
	File reassemble(UUID uploadId, String fileName);

	/**
	 * 지정된 이름의 파일을 삭제합니다.
	 * @param filePath 파일 경로
	 */
	void delete(String filePath) throws IOException;

}
