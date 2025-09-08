package dev.kyudong.back.post.application.port.in.event;

import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithGuestEvent;
import dev.kyudong.back.post.domain.dto.event.PostViewIncreaseWithUserEvent;

public interface PostViewEventUsecase {

	void increasePostViewWithUser(PostViewIncreaseWithUserEvent event);

	void increasePostViewWithGuest(PostViewIncreaseWithGuestEvent event);

}
