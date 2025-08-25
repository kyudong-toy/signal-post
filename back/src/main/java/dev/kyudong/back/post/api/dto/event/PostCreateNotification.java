package dev.kyudong.back.post.api.dto.event;

public record PostCreateNotification(
		Long postId,
		Long senderId
) {
}
