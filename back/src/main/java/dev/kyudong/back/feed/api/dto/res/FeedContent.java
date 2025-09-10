package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.feed.api.dto.PostFeedDto;
import dev.kyudong.back.post.domain.entity.PostStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record FeedContent(
		long postId,
		String subject,
		String content,
		long viewCount,
		long commentCount,
		PostStatus status,
		LocalDateTime createdAt,
		LocalDateTime modifiedAt
) {
	public static FeedContent from(PostFeedDto dto) {
		return new FeedContent(
				dto.postId(), dto.subject(), dto.content(),
				dto.viewCount(), dto.commentCount(), dto.status(),
				LocalDateTime.ofInstant(dto.createdAt(), ZoneOffset.UTC),
				LocalDateTime.ofInstant(dto.modifiedAt(), ZoneOffset.UTC)
		);
	}
}
