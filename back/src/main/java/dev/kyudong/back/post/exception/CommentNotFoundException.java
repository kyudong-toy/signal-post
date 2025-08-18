package dev.kyudong.back.post.exception;

public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException(final Long commentId) {
		super("Comment {" + commentId + "} Not Found");
	}
}
