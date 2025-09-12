package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record CommentContent(
		Long id,
		String content,
		CommentStatus status,
		LocalDateTime createdAt
) {
	public static CommentContent from(Comment comment) {
		return new CommentContent(
				comment.getId(),
				comment.getContent(),
				comment.getStatus(),
				LocalDateTime.ofInstant(comment.getCreatedAt(), ZoneOffset.UTC)
		);
	}
}
