package dev.kyudong.back.post.adapter.out.persistence.exception;

public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException(final Long commentId) {
		super("Comment {" + commentId + "} Not Found");
	}
}
