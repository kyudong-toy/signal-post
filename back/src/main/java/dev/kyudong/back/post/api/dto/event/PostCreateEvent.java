package dev.kyudong.back.post.api.dto.event;

import java.util.Set;

public record PostCreateEvent(
		Long postId,
		Set<Long> fileIds
) {
}
