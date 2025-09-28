package dev.kyudong.back.media.manager;

import dev.kyudong.back.media.exception.InvalidMediaFileException;
import dev.kyudong.back.media.exception.MediaFileStoredException;
import dev.kyudong.back.media.properties.MediaFileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class LocalMediaFileStorageManager implements MediaFileStorageManager {

	private final Path hostPath;
	private final Path tempDir;
	private final Path originDir;
	private final Path processDir;
	private final Path userDir;
	private final Path postDir;
	private final Path chatDir;

	public LocalMediaFileStorageManager(String hostPath, MediaFileStorageProperties.SubPaths subPaths) {
		try {
			this.hostPath = Paths.get(hostPath).normalize();
			this.tempDir = this.hostPath.resolve(subPaths.tempDir()).normalize();
			this.originDir = this.hostPath.resolve(subPaths.originDir()).normalize();
			this.processDir = this.hostPath.resolve(subPaths.processDir()).normalize();
			this.userDir = this.hostPath.resolve(subPaths.userDir()).normalize();
			this.postDir = this.hostPath.resolve(subPaths.postDir()).normalize();
			this.chatDir = this.hostPath.resolve(subPaths.chatDir()).normalize();

			initializeDirectories();

			log.info("MediaFileStorageManager 초기화 완료:");
			log.info("  - Host Directory: {}", this.hostPath);
			log.info("  - Temp Directory: {}", this.tempDir);
			log.info("  - User Directory: {}", this.userDir);
			log.info("  - Post Directory: {}", this.postDir);
			log.info("  - Chat Directory: {}", this.chatDir);
		} catch (IOException i) {
			log.error("LocalMediaFileStorageManager 초기화가 실패하였습니다...: hostPath={}", hostPath);
			throw new RuntimeException("파일 업로드 디렉토리 초기화 실패", i);
		}
	}

	private void initializeDirectories() throws IOException {
		validateDirPermissionsAndcreateDirectory(hostPath, "메인 저장소");
		validateDirPermissionsAndcreateDirectory(tempDir, "임시 저장소");
		validateDirPermissionsAndcreateDirectory(originDir, "원본 저장소");
		validateDirPermissionsAndcreateDirectory(processDir, "변환 작업후 저장소");
		validateDirPermissionsAndcreateDirectory(userDir, "사용자 저장소");
		validateDirPermissionsAndcreateDirectory(postDir, "게시글 저장소");
		validateDirPermissionsAndcreateDirectory(chatDir, "채팅 저장소");
	}

	private void validateDirPermissionsAndcreateDirectory(Path path, String description) throws IOException {
		if (Files.notExists(path)) {
			Files.createDirectories(path);
			if (!Files.isWritable(path)) {
				throw new IOException(String.format("%s에 쓰기 권한이 없습니다", description));
			}
			log.info("{} 디렉토리가 생성되었습니다: {}", description, path);
		} else {
			log.debug("{} 디렉토리가 이미 존재합니다: {}", description, path);
		}
	}

	@Override
	public void store(MultipartFile file, UUID uploadId, int chunkNumber) {
		try {
			Path tempDirPath = this.tempDir.resolve(uploadId.toString());
			Files.createDirectories(tempDirPath);
			Path destinationPath = tempDirPath.resolve("chunk_" + chunkNumber);

			file.transferTo(destinationPath);
			log.debug("파일이 정상적으로 업로드 되었습니다: path={}", destinationPath);
		} catch (IOException i) {
			log.error("파일 저장 중 예외 발생", i);
			throw new MediaFileStoredException();
		}
	}

	@Override
	public File reassemble(UUID uploadId, String fileName) {
		Path tempDir = this.tempDir.resolve(uploadId.toString());
		String newFileName = UUID.randomUUID() + "_" + fileName;
		Path finalPath = this.originDir.resolve(newFileName);

		File[] chunks = tempDir.toFile().listFiles((dir, name) -> name.startsWith("chunk_"));
		if (chunks == null || chunks.length == 0) {
			throw new InvalidMediaFileException("파일 업로드가 완료되지 않았습니다");
		}

		Arrays.sort(chunks, Comparator.comparingInt(f ->
				Integer.parseInt(f.getName().substring("chunk_".length()))
		));

		try (FileChannel destChannel = FileChannel.open(finalPath,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE)) {
			for (File chunk : chunks) {
				try (FileChannel sourceChannel = FileChannel.open(chunk.toPath(),
						StandardOpenOption.READ)) {
					destChannel.transferFrom(sourceChannel, destChannel.size(), sourceChannel.size());
				}
			}

			log.debug("파일 재조립 성공: finalPath={}", finalPath);
		} catch (FileNotFoundException f) {
			log.error("파일을 찾을 수 없습니다", f);
			throw new MediaFileStoredException();
		} catch (IOException i) {
			log.error("파일 저장중 에러가 발생했습니다", i);
			throw new MediaFileStoredException();
		} finally {
			cleanupTempDirectory(tempDir);
		}

		return finalPath.toFile();
	}

	/**
	 * 청크 파일을 모두 제거합니다
	 * @param tempDir 디렉토리
	 */
	private void cleanupTempDirectory(Path tempDir) {
		try (Stream<Path> walk = Files.walk(tempDir)) {
				walk.sorted(Comparator.reverseOrder())
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException i) {
							log.warn("임시 파일 삭제 실패: path={}", path, i);
						}
					});
		} catch (IOException e) {
			log.error("임시 파일 삭제 실패: {}", tempDir, e);
		}
	}

	@Override
	public void delete(String filePath) throws IOException {
		Path destinationPath =  Paths.get(filePath);

		Files.deleteIfExists(destinationPath);
		log.debug("파일이 삭제 되었습니다: path={}", destinationPath);
	}

}
