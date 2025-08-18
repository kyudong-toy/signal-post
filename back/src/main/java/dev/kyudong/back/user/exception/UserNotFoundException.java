package dev.kyudong.back.user.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(Long userId) {
		super("User {"+ userId + "} Not Found");
	}

	public UserNotFoundException(String username) {
		super("User: " + username + " Not Found");
	}
}
