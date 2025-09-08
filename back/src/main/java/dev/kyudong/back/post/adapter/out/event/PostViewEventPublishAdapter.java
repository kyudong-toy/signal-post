package dev.kyudong.back.post.adapter.out.event;

import dev.kyudong.back.post.application.port.out.event.PostViewEventPublishPort;
import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithGuestEvent;
import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithUserEvent;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostViewEventPublishAdapter implements PostViewEventPublishPort {

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void increasePostViewWithUser(User user, Post post) {
		PostViewIncreaseWithUserEvent event = new PostViewIncreaseWithUserEvent(user, post);
		applicationEventPublisher.publishEvent(event);
	}

	@Override
	public void increasePostViewWithGuest(String guestId, Post post) {
		PostViewIncreaseWithGuestEvent event = new PostViewIncreaseWithGuestEvent(guestId, post);
		applicationEventPublisher.publishEvent(event);
	}

}
