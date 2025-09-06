package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;

public record CommentStatusUpdateResDto(
		Long postId,
		Long commentId,
		CommentStatus status
) {
	public static CommentStatusUpdateResDto from(Comment comment) {
		return new CommentStatusUpdateResDto(
				comment.getPost().getId(), comment.getId(), comment.getStatus()
		);
	}
}
