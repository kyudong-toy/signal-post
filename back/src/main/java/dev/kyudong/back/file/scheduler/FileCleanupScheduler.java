package dev.kyudong.back.file.scheduler;

import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileStatus;
import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

	private final FileRepository fileRepository;
	private final FileStorageManager fileStorageManager;

	@Scheduled(cron = "0 0 4 * * *")
	@Transactional
	public void cleanupOrphanedPendingFiles() {
		log.info("오래된 임시(Pending) 파일 정리를 시작합니다........");

		// 현재 시점에서 72 시간 이전의 파일이 기준
		Instant threshold = Instant.now().minus(72, ChronoUnit.HOURS);

		List<File> orphanedPendingFiles = fileRepository
				.findByStatusAndCreatedAtBefore(FileStatus.PENDING, threshold);

		if (orphanedPendingFiles.isEmpty()) {
			log.info("정리할 임시 파일이 없습니다.........");
			return;
		}

		int pendingFilesize = orphanedPendingFiles.size();
		log.info("{}개의 임시 파일을 정리합니다!", pendingFilesize);

		for (File file : orphanedPendingFiles) {
			try {
				fileStorageManager.delete(file.getStoredFileName());
				log.info("파일 삭제 완료: fileId={}, storedFileName={}", file.getId(), file.getStoredFileName());
			} catch (Exception e) {
				log.error("파일 정리 종 오류가 발생: fileId={}", file.getId(), e);
			}
		}
		log.info("오래된 임시(Pending) 파일 {}개 정리가 끝났습니다!", pendingFilesize);
	}

}
