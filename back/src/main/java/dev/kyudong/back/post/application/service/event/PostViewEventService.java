package dev.kyudong.back.post.application.service.event;

import dev.kyudong.back.post.application.port.in.event.PostViewEventUsecase;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.application.port.out.web.PostViewPersistencePort;
import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithGuestEvent;
import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithUserEvent;
import dev.kyudong.back.post.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostViewEventService implements PostViewEventUsecase {

	private final PostViewPersistencePort postViewPersistencePort;
	private final PostUsecase postUsecase;
	private final StringRedisTemplate redisTemplate;

	@Override
	@Transactional
	public void increasePostViewWithUser(PostViewIncreaseWithUserEvent event) {
		log.debug("로그인 사용자 게시글 조회수 증가 이벤트를 실행합니다: event={}", event);

		postViewPersistencePort.increaseViewCount(event.user(), event.post());
		Post post = postUsecase.getPostEntityOrThrow(event.post().getId());
		post.increaseCount();
	}

	@Override
	@Transactional
	public void increasePostViewWithGuest(PostViewIncreaseWithGuestEvent event) {
		log.debug("비로그인 사용자 게시글 조회수 증가 이벤트를 실행합니다: event={}", event);

		String guestKey = "post:" + event.post().getId() + ":guestId:" + event.guestId();
		if (Boolean.FALSE.equals(redisTemplate.hasKey(guestKey))) {
			redisTemplate.opsForValue().set(guestKey, "viewed", 72, TimeUnit.HOURS);
			Post post = postUsecase.getPostEntityOrThrow(event.post().getId());
			post.increaseCount();
		}
	}

}
