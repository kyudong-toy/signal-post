package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.user.domain.User;

public record CommentAuthor(
		Long id,
		String username
) {
	public static CommentAuthor from(Comment comment) {
		User user = comment.getUser();
		return new CommentAuthor(user.getId(), user.getUsername());
	}
}
