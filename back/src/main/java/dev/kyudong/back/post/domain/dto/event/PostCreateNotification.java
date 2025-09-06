package dev.kyudong.back.post.domain.dto.event;

public record PostCreateNotification(
		Long postId,
		Long senderId
) {
}
