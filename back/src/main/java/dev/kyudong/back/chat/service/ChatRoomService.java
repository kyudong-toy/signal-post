package dev.kyudong.back.chat.service;

import dev.kyudong.back.chat.api.dto.event.ChatRoomCreateEvent;
import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UsersNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.service.UserReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatEventHandler chatEventHandler;
	private final UserReaderService userReaderService;

	@Transactional(readOnly = true)
	public ChatRoomResDto findChatRooms(final Long userId, Long lastChatroomId, Instant cursorTime) {
		log.debug("채팅방 목록을 조회합니다: userId={}", userId);

		User user = userReaderService.getUserReference(userId);
		PageRequest pageRequest = PageRequest.of(0, 10);

		Slice<ChatRoom> chatRooms = (lastChatroomId == null)
				? chatRoomRepository.findChatRooomsByMember(user, pageRequest)
				: chatRoomRepository.findChatRooomsByMember(user, lastChatroomId, cursorTime, pageRequest);

		return ChatRoomResDto.from(chatRooms);
	}

	@Transactional
	public ChatRoomCreateResDto createChatRoom(final Long userId, ChatRoomCreateReqDto request) {
		log.debug("채팅방 생성합니다: userId={}", userId);

		List<User> userList = userReaderService.getUsersByIds(request.userIds());
		if (userList.isEmpty()) {
			throw new UsersNotFoundException();
		}

		ChatRoom newChatRoom = ChatRoom.builder()
				.initUserList(userList)
				.build();

		ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

		chatEventHandler.handleRoomCreate(ChatRoomCreateEvent.from(savedChatRoom));

		log.debug("채팅방 생성이 완료되었습니다: userId={}", userId);
		return ChatRoomCreateResDto.from(savedChatRoom);
	}

}
