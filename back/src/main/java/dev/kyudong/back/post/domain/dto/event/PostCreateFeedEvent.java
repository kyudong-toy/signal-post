package dev.kyudong.back.post.domain.dto.event;


import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;

public record PostCreateFeedEvent(
		Post post,
		User author
) {
}
