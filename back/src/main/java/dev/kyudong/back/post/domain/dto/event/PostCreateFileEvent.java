package dev.kyudong.back.post.domain.dto.event;

import java.util.Set;

public record PostCreateFileEvent(
		Long postId,
		Set<Long> fileIds
) {
}
