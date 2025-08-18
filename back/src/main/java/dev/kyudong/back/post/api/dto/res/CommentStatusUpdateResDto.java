package dev.kyudong.back.post.api.dto.res;

import dev.kyudong.back.post.domain.Comment;
import dev.kyudong.back.post.domain.CommentStatus;

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
