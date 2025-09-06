package dev.kyudong.back.post.domain.dto.event;

import java.util.Set;

public record PostUpdateFileEvent(
		Long postId,
		Set<Long> fileIds
) {
}
