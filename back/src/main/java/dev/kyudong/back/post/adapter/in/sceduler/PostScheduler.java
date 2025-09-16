package dev.kyudong.back.post.adapter.in.sceduler;

import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostScheduler {

	private final PostUsecase postUsecase;

	@Scheduled(cron = "0 0 */3 * * *")
	public void refreshRandomOldPost() {
		log.info("오래된 게시글 목록을 랜덤으로 생성합니다");

		postUsecase.refreshRandomOldPost();

		log.info("오래된 게시글 목록을 랜덤으로 생성이 완료되었습니다");
	}

}
