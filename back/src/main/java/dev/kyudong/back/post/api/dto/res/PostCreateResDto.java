package dev.kyudong.back.post.api.dto.res;

import dev.kyudong.back.post.domain.Post;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record PostCreateResDto(
		long postId,
		String subject,
		String content,
		LocalDateTime createdAt,
		LocalDateTime modifiedAt
) {
	public static PostCreateResDto from(Post post) {
		return new PostCreateResDto(
				post.getId(), post.getSubject(), post.getContent(),
				LocalDateTime.ofInstant(post.getCreatedAt(), ZoneOffset.UTC),
				LocalDateTime.ofInstant(post.getModifiedAt(), ZoneOffset.UTC)
		);
	}
}
