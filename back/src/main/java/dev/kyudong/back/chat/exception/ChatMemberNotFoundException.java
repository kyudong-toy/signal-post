package dev.kyudong.back.chat.exception;

public class ChatMemberNotFoundException extends RuntimeException {
	public ChatMemberNotFoundException(Long userId, Long chatroomId) {
		super(String.format("채팅방에 존재하지 않는 사용자입니다: userId=%d, chatroomId=%d", userId, chatroomId));
	}
}
