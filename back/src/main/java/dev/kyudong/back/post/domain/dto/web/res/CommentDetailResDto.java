package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record CommentDetailResDto(
		Long postId,
		Long userId,
		Long commentId,
		String content,
		CommentStatus status,
		LocalDateTime createdAt,
		LocalDateTime modifiedAt
) {
	public static CommentDetailResDto from(Comment comment) {
		return new CommentDetailResDto(
				comment.getPost().getId(), comment.getUser().getId(),
				comment.getId(), comment.getContent(), comment.getStatus(),
				LocalDateTime.ofInstant(comment.getCreatedAt(), ZoneOffset.UTC),
				LocalDateTime.ofInstant(comment.getModifiedAt(), ZoneOffset.UTC)
		);
	}
}
