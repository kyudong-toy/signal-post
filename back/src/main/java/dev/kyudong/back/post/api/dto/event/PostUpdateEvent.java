package dev.kyudong.back.post.api.dto.event;

import java.util.Set;

public record PostUpdateEvent(
		Long postId,
		Set<Long> fileIds,
		Set<Long> delFileIds
) {
}
