package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Post;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record PostUpdateResDto(
		long postId,
		String subject,
		String content,
		LocalDateTime createdAt,
		LocalDateTime modifiedAt
) {
	public static PostUpdateResDto from(Post post) {
		return new PostUpdateResDto(
				post.getId(), post.getSubject(), post.getContent(),
				LocalDateTime.ofInstant(post.getCreatedAt(), ZoneOffset.UTC),
				LocalDateTime.ofInstant(post.getModifiedAt(), ZoneOffset.UTC)
		);
	}
}
