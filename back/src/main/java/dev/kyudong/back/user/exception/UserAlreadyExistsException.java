package dev.kyudong.back.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
	public UserAlreadyExistsException(String username) {
		super(username + " Already Exists");
	}
}
