package dev.kyudong.back.media.scheduler;

import dev.kyudong.back.media.domain.MediaFile;
import dev.kyudong.back.media.domain.MediaFileStatus;
import dev.kyudong.back.media.manager.MediaFileStorageManager;
import dev.kyudong.back.media.repository.MediaFileRepository;
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
public class MediaFileCleanupScheduler {

	private final MediaFileRepository mediaFileRepository;
	private final MediaFileStorageManager mediaFileStorageManager;

	@Scheduled(cron = "0 0 4 * * *")
	@Transactional
	public void cleanupOrphanedPendingFiles() {
		log.info("오래된 임시(Pending) 파일 정리를 시작합니다........");

		// 현재 시점에서 72 시간 이전의 파일이 기준
		Instant threshold = Instant.now().minus(72, ChronoUnit.HOURS);

		List<MediaFile> orphanedPendingMediaFiles = mediaFileRepository
				.findByStatusAndCreatedAtBefore(MediaFileStatus.PENDING, threshold);

		if (orphanedPendingMediaFiles.isEmpty()) {
			log.info("정리할 임시 파일이 없습니다.........");
			return;
		}

		int pendingFilesize = orphanedPendingMediaFiles.size();
		log.info("{}개의 임시 파일을 정리합니다!", pendingFilesize);

		for (MediaFile mediaFile : orphanedPendingMediaFiles) {
			try {
				mediaFileStorageManager.delete(mediaFile.getFilePath());
				log.info("파일 삭제 완료: fileId={}, storedFileName={}", mediaFile.getId(), mediaFile.getStoredFileName());
			} catch (Exception e) {
				log.error("파일 정리 종 오류가 발생: fileId={}", mediaFile.getId(), e);
			}
		}
		log.info("오래된 임시(Pending) 파일 {}개 정리가 끝났습니다!", pendingFilesize);
	}

}
