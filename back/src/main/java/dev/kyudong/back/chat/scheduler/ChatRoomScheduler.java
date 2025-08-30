package dev.kyudong.back.chat.scheduler;

import dev.kyudong.back.chat.repository.ChatRoomRepository;
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
public class ChatRoomScheduler {

	private final ChatRoomRepository chatRoomRepository;

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupOrphanedChatRoom() {
		log.info("이용자가 없는 채팅방을 삭제처리합니다........");

		// 일주일 기준 이용자가 없는 채팅룸 삭제
		Instant threshold = Instant.now().minus(7, ChronoUnit.DAYS);
		List<Long> orphanedChatRoomIds = chatRoomRepository.findByOrphanedChatroomIds(threshold);

		if (orphanedChatRoomIds.isEmpty()) {
			log.info("정리할 채팅방이 없습니다.........");
			return;
		}

		int chatRoomSize = orphanedChatRoomIds.size();
		log.info("{}개의 채팅방을 정리합니다!", chatRoomSize);

		chatRoomRepository.updateOrphanedChatroom(orphanedChatRoomIds);
		log.info("오래된 알림 {}개의 정리가 완료되었습니다", chatRoomSize);
	}

}
