package dev.kyudong.back.media.processer;

import dev.kyudong.back.media.api.dto.event.ProcessingPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 파일 작업 완료를 보내주는 클래스입니다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaProcessSender {

	private final SimpMessagingTemplate simpMessagingTemplate;

	/**
	 * 파일의 진행 상태를 전송합니다
	 * @param payload	요청 정보
	 */
	public void sendMediaFileProgress(ProcessingPayload payload) {
		try {
			String uploaderId = String.valueOf(payload.uploaderId());
			String destinationQueue = "/queue/progress";

			simpMessagingTemplate.convertAndSendToUser(uploaderId, destinationQueue, payload);

			log.debug("파일 처리 상태 전송: user={}, payload={}", uploaderId, payload);

//			String topicDestination = "/topic/progress/" + userId;
//			simpMessagingTemplate.convertAndSend(topicDestination, payload);
		} catch (Exception e) {
			log.error("파일 처리 상태 전송 실패: uploaderId={}", payload.uploaderId(), e);
		}
	}

}
