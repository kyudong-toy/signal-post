package dev.kyudong.back.media.config;

import dev.kyudong.back.media.manager.MediaFileStorageManager;
import dev.kyudong.back.media.manager.LocalMediaFileStorageManager;
import dev.kyudong.back.media.properties.MediaFileStorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaFileStroageMangerConfig {

	@Bean
	public MediaFileStorageManager LocalMediaFileStorageManager(MediaFileStorageProperties mediaFileStorageProperties) {
		return new LocalMediaFileStorageManager(mediaFileStorageProperties.hostPath(), mediaFileStorageProperties.subPaths());
	}

}
