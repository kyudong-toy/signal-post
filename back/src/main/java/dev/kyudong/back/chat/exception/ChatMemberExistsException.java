package dev.kyudong.back.chat.exception;

public class ChatMemberExistsException extends RuntimeException {
	public ChatMemberExistsException(Long userId) {
		super("이미 채팅방에 초대된 사용자입니다: userId=" + userId);
	}
}
