package dev.kyudong.back.file.config;

import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.manager.LocalFileStorageManager;
import dev.kyudong.back.file.properties.FileStorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStroageMangerConfig {

	@Bean
	public FileStorageManager LocalFileStorageManager(FileStorageProperties fileStorageProperties) {
		return new LocalFileStorageManager(fileStorageProperties.getUploadDir());
	}

}
