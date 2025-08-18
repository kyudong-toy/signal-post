package dev.kyudong.back.post.exception;

public class PostNotFoundException extends RuntimeException {
	public PostNotFoundException(Long postId) {
		super("Post {" + postId + "} Not Found");
	}
}
