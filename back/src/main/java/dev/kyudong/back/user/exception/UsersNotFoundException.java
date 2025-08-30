package dev.kyudong.back.user.exception;

public class UsersNotFoundException extends RuntimeException {
	public UsersNotFoundException() {
		super("요청한 사용자 목록을 조회할 수 없습니다");
	}
}
