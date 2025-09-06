package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record PostDetailResDto(
		long postId,
		long userId,
		String subject,
		String content,
		PostStatus status,
		LocalDateTime createdAt,
		LocalDateTime modifiedAt
) {
	public static PostDetailResDto from(Post post) {
		return new PostDetailResDto(
				post.getId(), post.getUser().getId(), post.getSubject(), post.getContent(), post.getStatus(),
				LocalDateTime.ofInstant(post.getCreatedAt(), ZoneOffset.UTC),
				LocalDateTime.ofInstant(post.getModifiedAt(), ZoneOffset.UTC)
		);
	}
}
