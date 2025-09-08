package dev.kyudong.back.post.domain.dto.event;

import dev.kyudong.back.post.domain.entity.Post;

public record PostViewIncreaseWithGuestEvent(
		String guestId,
		Post post
) {
}
