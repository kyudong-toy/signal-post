package dev.kyudong.back.chat.event;

import dev.kyudong.back.chat.api.dto.event.*;
import dev.kyudong.back.chat.api.dto.ws.*;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.MemberStatus;
import dev.kyudong.back.chat.websocket.ChatBroadCaster;
import dev.kyudong.back.chat.websocket.ChatWebSocketMessage;
import dev.kyudong.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultChatEventHandler implements ChatEventHandler {

	private final ChatBroadCaster chatBroadCaster;

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleMessageCreate(ChatMessageCreateEvent event) {
		log.debug("채팅 메시지 생성 이벤트를 수신하였습니다: roomId={}", event.roomId());

		ChatWebSocketMessage<ChatNewMessageWsDto> webSocketMessage =
				ChatWebSocketMessage.of(ChatEventType.NEW_MESSAGE, ChatNewMessageWsDto.from(event));

		Set<Long> joinUserIds = event.members().stream()
				.filter(m -> m.getStatus() == MemberStatus.JOINED)
				.map(ChatMember::getUser)
				.map(User::getId)
				.collect(Collectors.toSet());

		chatBroadCaster.brodCastMessageToMembers(joinUserIds, webSocketMessage);
		log.debug("채팅 메시지 생성 이벤트가 종료되었습니다");
	}

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleMessageDelete(ChatMessageDeleteEvent event) {
		log.debug("채팅 메시지 삭제 이벤트를 수신하였습니다: roomId={}", event.roomId());

		ChatWebSocketMessage<ChatDelMessageWsDto> webSocketMessage =
				ChatWebSocketMessage.of(ChatEventType.DEL_MESSAGE, ChatDelMessageWsDto.from(event));

		Set<Long> joinUserIds = event.members().stream()
				.filter(m -> m.getStatus() == MemberStatus.JOINED)
				.map(ChatMember::getUser)
				.map(User::getId)
				.collect(Collectors.toSet());;

		chatBroadCaster.brodCastMessageToMembers(joinUserIds, webSocketMessage);
		log.debug("채팅 메시지 삭제 이벤트가 종료되었습니다");
	}

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleRoomCreate(ChatRoomCreateEvent event) {
		log.debug("채팅방 생성 이벤트를 수신하였습니다: roomId={}", event.roomId());

		Map<Long, ChatWebSocketMessage<ChatNewRoomWsDto>> webSocketMessage = new HashMap<>();
		event.chatMembers().forEach(member -> {
			ChatNewRoomWsDto chatNewRoomWsDto = ChatNewRoomWsDto.of(event, member);
			webSocketMessage.put(member.getId(), ChatWebSocketMessage.of(ChatEventType.NEW_CHAT_ROOM, chatNewRoomWsDto));
		});

		chatBroadCaster.brodCastIndividualMessages(webSocketMessage);
		log.debug("채팅방 생성 이벤트가 종료되었습니다");
	}

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleMemberInvite(ChatMemberInviteEvent event) {
		log.debug("사용자 초대 이벤트를 수신하였습니다: roomId={}", event.roomId());

		// 초대된 사용자들에게 메시지 전송
		Map<Long, ChatWebSocketMessage<ChatMemberInviteWsDto>> webSocketInvidualMessage = new HashMap<>();
		event.newMembers().forEach(member -> {
			ChatMemberInviteWsDto chatNewRoomWsDto = ChatMemberInviteWsDto.of(event, member);
			webSocketInvidualMessage.put(member.getUser().getId(), ChatWebSocketMessage.of(ChatEventType.INVITE_NEW_MEMBER, chatNewRoomWsDto));
		});
		chatBroadCaster.brodCastIndividualMessages(webSocketInvidualMessage);

		// 방을 이용중인 사용자들에게 메시지 전송
		ChatWebSocketMessage<ChatNewMemberWsDto> webSocketSameMessage =
				ChatWebSocketMessage.of(ChatEventType.NEW_MEMBER, ChatNewMemberWsDto.from(event));
		chatBroadCaster.brodCastMessageToMembers(event.existsUserIds(), webSocketSameMessage);
		log.debug("사용자 초대 이벤트가 종료되었습니다");
	}

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleMemberLeft(ChatMemberLeftEvent event) {
		log.debug("채팅방 퇴장 이벤트를 수신하였습니다: roomId={}", event.roomId());

		ChatWebSocketMessage<ChatMemberLeftWsDto> webSocketMessage =
				ChatWebSocketMessage.of(ChatEventType.LEFT_MEMBER, ChatMemberLeftWsDto.from(event));

		Set<Long> joinUserIds = event.members().stream()
				.filter(m -> m.getStatus() == MemberStatus.JOINED)
				.map(ChatMember::getUser)
				.map(User::getId)
				.filter(userId -> !event.leaveUserId().equals(userId))
				.collect(Collectors.toSet());;

		chatBroadCaster.brodCastMessageToMembers(joinUserIds, webSocketMessage);
		log.debug("채팅방 퇴장 이벤트가 종료되었습니다");
	}

}
