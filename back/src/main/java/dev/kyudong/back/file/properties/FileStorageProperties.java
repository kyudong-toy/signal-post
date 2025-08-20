package dev.kyudong.back.file.properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Slf4j
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

	private final String uploadDir;
	private final String publicWebPath;

	@ConstructorBinding
	public FileStorageProperties(String uploadDir, String publicWebPath) {
		log.debug("파일 저장소 설정합니다: uploadDir={}, publicWebPath={}", uploadDir, publicWebPath);
		this.uploadDir = uploadDir;
		this.publicWebPath = publicWebPath;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public String getPublicWebPath() {
		return publicWebPath;
	}

}
