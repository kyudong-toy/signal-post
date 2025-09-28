package dev.kyudong.back.media.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "media")
public record MediaFileStorageProperties(
		String basePath,
		String webPath,
		String hostPath,
		SubPaths subPaths
) {
	public record SubPaths(
			String tempDir,
			String originDir,
			String processDir,
			String userDir,
			String postDir,
			String chatDir
	) {}
}