package dev.kyudong.back.chat.service;

import dev.kyudong.back.chat.api.dto.event.ChatMemberInviteEvent;
import dev.kyudong.back.chat.api.dto.event.ChatMemberLeftEvent;
import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.MemberStatus;
import dev.kyudong.back.chat.domain.RoomStatus;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UsersNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemberService {

	private final ChatMemberRepository chatMemberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public void inviteChatRoom(final Long userId, final Long roomId, ChatMemberInviteReqDto requset) {
		log.debug("채팅방에 새로운 사용자를 추가합니다: roomId={}", roomId);

		ChatRoom chatRoom = chatRoomRepository.findChatroomByIdAndStatus(roomId, RoomStatus.ACTIVE)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 채팅방입니다: roomId={}", roomId);
					return new ChatRoomNotFoundException(roomId);
				});

		Set<Long> existsUserIds = chatMemberRepository.findExistsMemberUserIds(
				requset.userIds(), chatRoom, MemberStatus.JOINED
		);

		if (!existsUserIds.contains(userId)) {
			throw new ChatMemberNotFoundException(userId, roomId);
		}

		Set<Long> newUserIds = requset.userIds().stream()
				.filter(id -> !existsUserIds.contains(id))
				.collect(Collectors.toSet());

		List<User> userList = userRepository.findByIdIn(newUserIds);
		if (userList.isEmpty()) {
			throw new UsersNotFoundException();
		}

		Set<ChatMember> newMembers = chatRoom.addNewMember(userList);

		applicationEventPublisher.publishEvent(ChatMemberInviteEvent.of(chatRoom, existsUserIds, newMembers));
		log.info("새로운 사용자가 채팅방에 참여하였습니다: roomId={}", roomId);
	}

	@Transactional
	public void leaveChatRoom(final Long roomId, final Long leaveUserId) {
		log.debug("채팅방의 사용자가 채팅방을 탈퇴합니다: roomId={}, leaveUserId={}", roomId, leaveUserId);

		ChatRoom chatRoom = chatRoomRepository.findChatroomByIdAndStatus(roomId, RoomStatus.ACTIVE)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 채팅방입니다: roomId={}", roomId);
					return new ChatRoomNotFoundException(roomId);
				});

		User leaveUser = userRepository.getReferenceById(leaveUserId);
		ChatMember chatMember = chatMemberRepository.findByUserAndChatRoom(leaveUser, chatRoom)
				.orElseThrow(() -> {
					log.warn("채팅방에 존재하지 않는 사용자입니다: roomId={}, leaveUserId={}", roomId, leaveUserId);
					return new ChatMemberNotFoundException(leaveUserId, roomId);
				});

		chatMember.leave();

		applicationEventPublisher.publishEvent(ChatMemberLeftEvent.of(roomId, leaveUserId, chatRoom.getChatMembers()));
		log.info("사용자가 채팅방을 떠났습니다: roomId={}, leaveUserId={}", roomId, leaveUserId);
	}

	@Transactional(readOnly = true)
	public boolean isChatMember(Long roomId, String username) {
		return chatMemberRepository.existsByChatMember(roomId, username);
	}

}
