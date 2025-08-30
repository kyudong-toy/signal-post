package dev.kyudong.back.chat.exception;

public class ChatMessageNotFoundException extends RuntimeException {
	public ChatMessageNotFoundException(Long messageId, Long chatroomId) {
		super(String.format("존재하지 않는 메시지입니다: messageId=%d, chatroomId=%d", messageId, chatroomId));
	}
}
