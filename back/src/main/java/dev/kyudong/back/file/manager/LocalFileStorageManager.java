package dev.kyudong.back.file.manager;

import dev.kyudong.back.file.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class LocalFileStorageManager implements FileStorageManager {

	private final Path uploadPath;

	public LocalFileStorageManager(final String uploadDir) {
		try {
			this.uploadPath = Paths.get(uploadDir);
			if (Files.notExists(uploadPath)) {
				log.info("디렉토리가 생성 되었습니다: path={}", uploadPath);
				Files.createDirectories(uploadPath);
			}
		} catch (IOException i) {
			log.error("LocalFileStorageManager 초기화가 실패하였습니다...: uploadDir={}", uploadDir);
			throw new RuntimeException("파일 업로드 디렉토리 초기화 실패", i);
		}
	}

	@Override
	public String store(MultipartFile multipartFile, String storedFileName) throws IOException {
		if (storedFileName.contains("..")) {
			log.error("파일 이름에 부적절한 문자가 포함되어 있습니다: storedFileName={} ", storedFileName);
			throw new InvalidFileException("파일 이름에 부적절한 문자가 포함되어 있습니다.");
		}

		Path destinationPath = this.uploadPath.resolve(storedFileName);
		log.debug("파일 업로드를 시작합니다: destinationPath={}", destinationPath);

		// 파일 업로드
		multipartFile.transferTo(destinationPath.toFile());

		log.debug("파일이 정상적으로 업로드 되었습니다: path={}, storedFileName={}", destinationPath, storedFileName);
		return destinationPath.toString();
	}

	@Override
	public void delete(String storedFileName) throws IOException {
		Path destinationPath = this.uploadPath.resolve(storedFileName);
		log.debug("파일 삭제를 시작합니다: destinationPath={}", destinationPath);

		Files.deleteIfExists(destinationPath);
		log.info("파일이 정상적으로 삭제 되었습니다: path={}, storedFileName={}", destinationPath, storedFileName);
	}

}
