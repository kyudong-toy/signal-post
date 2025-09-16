package dev.kyudong.back.user.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(Long userId) {
		super("USER {"+ userId + "} Not Found");
	}

	public UserNotFoundException(String username) {
		super("USER {" + username + "} Not Found");
	}
}
