package dev.kyudong.back.chat.event;

import dev.kyudong.back.chat.api.dto.event.*;

public interface ChatEventHandler {

	/**
	 * 새로운 채팅 메시지가 생성되었다는 이벤트를 처리하며,
	 * 해당 채팅방의 모든 참여자에게 웹소켓을 통해 메시지를 전송한다.
	 * @see ChatMessageCreateEvent
	 * @param event 메시지 생성
	 */
	void handleMessageCreate(ChatMessageCreateEvent event);

	/**
	 * 채팅 메시지가 삭제되었다는 이벤트를 처리하며,
	 * 해당 채팅방의 모든 참여자에게 웹소켓을 통해 삭제된 메시지 상태를 전송한다.
	 * @see ChatMessageDeleteEvent
	 * @param event 메시지 삭제
	 */
	void handleMessageDelete(ChatMessageDeleteEvent event);

	/**
	 * 채팅방 생성 이벤트를 처리하며,
	 * 초대된 사용자에게 웹소켓을 통해 채팅방 정보를 전송한다.
	 * @see ChatRoomCreateEvent
	 * @param event 채팅방 생성
	 */
	void handleRoomCreate(ChatRoomCreateEvent event);

	/**
	 * 채팅방에 사용자 초대 이벤트를 처리하며,
	 * 초대된 사용자에게 웹소켓을 통해 채팅방 정보를 전송한다.
	 * @see ChatMemberInviteEvent
	 * @param event 초대된 사용자
	 */
	void handleMemberInvite(ChatMemberInviteEvent event);

	/**
	 * 채팅방에 사용자 탈퇴 이벤트를 처리하며,
	 * 채티방의 사용자들에게 웹소켓을 통해 채팅방 정보를 전송한다.
	 * @see ChatMemberLeftEvent
	 * @param event 채팅방 탈퇴 사용자
	 */
	void handleMemberLeft(ChatMemberLeftEvent event);

}
