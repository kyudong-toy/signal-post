package dev.kyudong.back.file.service;

import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileOwnerType;
import dev.kyudong.back.file.exception.FileMetadataNotFoundException;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.post.domain.dto.event.PostCreateFileEvent;
import dev.kyudong.back.post.domain.dto.event.PostUpdateFileEvent;
import dev.kyudong.back.user.api.dto.event.UserImageDeleteEvent;
import dev.kyudong.back.user.api.dto.event.UserImagePublishEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileEventService {

	private final FileRepository fileRepository;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleUserImagePublishEvent(UserImagePublishEvent event) {
		final Long userId = event.userId();
		final Long fileId = event.fileId();
		log.debug("사용자 이미지 생성 이벤트 수신완료: userId={}", userId);
		fileRepository.confirmFileById(fileId, userId, FileOwnerType.USER);
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handleUserImageDeleteEvent(UserImageDeleteEvent event) {
		final Long userId = event.userId();
		final Long prevFileId = event.prevFileId();
		log.debug("사용자 이미지 생성 이벤트 수신완료: userId={}", userId);

		// 기존에 저장된 이미지를 삭제한다
		File userImage = fileRepository.findByIdAndOwnerId(prevFileId, userId)
				.orElseThrow(() -> {
					log.warn("사용자 이미지를 찾을 수 없습니다: prevFileId={}, userId={}", prevFileId, userId);
					return new FileMetadataNotFoundException(prevFileId);
				});
		userImage.deleteFile();
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handlePostCreateEvent(PostCreateFileEvent event) {
		final Long postId = event.postId();
		log.debug("게시글 생성 이벤트 수신완료: postId={}", postId);

		// 파일들을 활성화 상태로 변경한다.
		Set<Long> fileIds = event.fileIds();
		if (!fileIds.isEmpty()) {
			log.debug("게시글({})에 파일을 추가합니다: fileIds:{}", postId, fileIds);
			fileRepository.confirmFileByIds(fileIds, postId, FileOwnerType.POST);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handlePostUpdateEvent(PostUpdateFileEvent event) {
		final Long postId = event.postId();
		log.info("게시글 수정 이벤트 수신완료: postId={}", postId);

		// 사용하지 않는 파일들을 삭제 처리
		// todo : 기존 파일 목록 로드 로직 필요

		// 파일들을 활성화 상태로 변경한다.
		Set<Long> fileIds = event.fileIds();
		if (!fileIds.isEmpty()) {
			log.debug("게시글({})에 파일을 추가합니다: fileIds:{}", postId, fileIds);
			fileRepository.confirmFileByIds(fileIds, postId, FileOwnerType.POST);
		}
	}

}

