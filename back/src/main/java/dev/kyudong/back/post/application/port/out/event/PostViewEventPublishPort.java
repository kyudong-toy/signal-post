package dev.kyudong.back.post.application.port.out.event;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;

public interface PostViewEventPublishPort {

	void increasePostViewWithUser(User user, Post post);

	void increasePostViewWithGuest(String guestId, Post post);

}
