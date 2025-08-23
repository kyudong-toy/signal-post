package dev.kyudong.back.post.api.dto.event;


import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.user.domain.User;

public record PostCreateFeedEvent(
		Post post,
		User author
) {
}
