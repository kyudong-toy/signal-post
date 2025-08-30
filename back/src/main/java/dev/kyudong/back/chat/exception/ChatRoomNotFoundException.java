package dev.kyudong.back.chat.exception;

public class ChatRoomNotFoundException extends RuntimeException {
	public ChatRoomNotFoundException(Long chatRoomId) {
		super("존재하지 않는 채팅방입니다: chatRoomId=" + chatRoomId);
	}
}
