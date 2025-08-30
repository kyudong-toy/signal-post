package dev.kyudong.back.chat.event;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 웹 소켓과 채팅간의 이벤트를 정의합니다
 */
public enum ChatEventType {

	NEW_MESSAGE("newMessage"),
	DEL_MESSAGE("delMessage"),
	NEW_CHAT_ROOM("newChatRoom"),
	INVITE_NEW_MEMBER("inviteNewMember"),
	NEW_MEMBER("newMember"),
	LEFT_MEMBER("leftMember");

	private final String detail;

	ChatEventType(String deatil) {
		this.detail = deatil;
	}

	@JsonValue
	public String getDetail() {
		return detail;
	}

}
