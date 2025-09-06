package dev.kyudong.back.post.adapter.out.event;

import dev.kyudong.back.post.application.port.out.PostEventPublishPort;
import dev.kyudong.back.post.domain.dto.event.PostCreateFeedEvent;
import dev.kyudong.back.post.domain.dto.event.PostCreateFileEvent;
import dev.kyudong.back.post.domain.dto.event.PostCreateNotification;
import dev.kyudong.back.post.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventPublishAdapter implements PostEventPublishPort {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void postCreateEventPublish(Post post, Set<Long> fileIds) {
		log.debug("게시글 생성 이벤트를 시작합니다");

		if (!fileIds.isEmpty()) {
			PostCreateFileEvent fileEvent = new PostCreateFileEvent(post.getId(), fileIds);
			applicationEventPublisher.publishEvent(fileEvent);
		}

		// feed 추가
		PostCreateFeedEvent feedEvent = new PostCreateFeedEvent(post, post.getUser());
		applicationEventPublisher.publishEvent(feedEvent);

		// 알림 이벤트 추가
		PostCreateNotification notificationEvent = new PostCreateNotification(post.getId(), post.getUser().getId());
		applicationEventPublisher.publishEvent(notificationEvent);
	}

	@Override
	public void postUpdateEventPublish(Post post, Set<Long> fileIds) {
		log.debug("게시글 수정 이벤트를 시작합니다");

		if (!fileIds.isEmpty()) {
			PostCreateFileEvent fileEvent = new PostCreateFileEvent(post.getId(), fileIds);
			applicationEventPublisher.publishEvent(fileEvent);
		}

		// feed 추가
		PostCreateFeedEvent feedEvent = new PostCreateFeedEvent(post, post.getUser());
		applicationEventPublisher.publishEvent(feedEvent);

		// 알림 이벤트 추가
		PostCreateNotification notificationEvent = new PostCreateNotification(post.getId(), post.getUser().getId());
		applicationEventPublisher.publishEvent(notificationEvent);
	}

}
