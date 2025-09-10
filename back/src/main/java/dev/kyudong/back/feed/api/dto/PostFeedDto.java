package dev.kyudong.back.feed.api.dto;

import dev.kyudong.back.post.domain.entity.PostStatus;

import java.time.Instant;

public record PostFeedDto(
		Long postId,
		Long authorId,
		String username,
		String subject,
		String content,
		PostStatus status,
		long viewCount,
		long commentCount,
		double score,
		Instant createdAt,
		Instant modifiedAt
) {
}
