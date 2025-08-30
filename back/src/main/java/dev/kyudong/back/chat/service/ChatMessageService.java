package dev.kyudong.back.chat.service;

import dev.kyudong.back.chat.api.dto.event.ChatMessageDeleteEvent;
import dev.kyudong.back.chat.api.dto.event.ChatMessageCreateEvent;
import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatMessage;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.exception.ChatMessageNotFoundException;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.chat.repository.ChatMessageRepository;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMemberRepository chatMemberRepository;
	private final UserRepository userRepository;
	private final ChatEventHandler chatEventHandler;

	@Transactional(readOnly = true)
	public ChatMessageResDto findMessages(final Long roomId, Long cursorId, Instant cursorTime) {
		log.debug("채팅 메시지를 조회합니다: roomId={}", roomId);

		PageRequest pageRequest = PageRequest.of(0, 10);

		Slice<ChatMessage> chatMessages = (cursorTime == null)
				? chatMessageRepository.findChatMessage(roomId, pageRequest)
				: chatMessageRepository.findChatMessageByCursorTime(roomId, cursorId, cursorTime, pageRequest);

		return ChatMessageResDto.from(chatMessages);
	}

	@Transactional
	public void createMessage(final Long userId, final Long roomId, ChatMessageCreateReqDto request) {
		log.debug("사용자가 메시지를 전송합니다: userId={}, roomId={}", userId, roomId);

		User user = userRepository.getReferenceById(userId);
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> {
					log.warn("메시지를 전송한 채팅방이 존재하지 않습니다: roomId={}", roomId);
					return new ChatRoomNotFoundException(roomId);
				});

		ChatMember sender = chatMemberRepository.findByUserAndChatRoom(user, chatRoom)
				.orElseThrow(() -> {
					log.warn("채팅방에 없는 사용자입니다: roomId={}, userId={}", roomId, userId);
					return new ChatMemberNotFoundException(userId, roomId);
				});

		ChatMessage newChatMessage =  ChatMessage.builder()
				.sender(sender)
				.content(request.content())
				.messageType(request.messageType())
				.build();
		chatRoom.addMessage(newChatMessage);
		ChatMessage savedChatMessage = chatMessageRepository.save(newChatMessage);

		chatEventHandler.handleMessageCreate(ChatMessageCreateEvent.of(savedChatMessage, chatRoom, sender.getUser()));
		int size = chatRoom.getChatMembers().size();
		log.info("메시지를 전송하였습니다: userId={}, roomId={}, size={}", userId, roomId, size);
	}

	@Transactional
	public void deleteMessage(final Long userId, final Long roomId, final Long messageId) {
		log.debug("사용자의 메시지를 삭제합니다: userId={}, roomId={}", userId, roomId);

		User user = userRepository.getReferenceById(userId);
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> {
					log.warn("메시지를 삭제할 채팅방이 존재하지 않습니다: roomId={}", roomId);
					return new ChatRoomNotFoundException(roomId);
				});

		ChatMember sender = chatMemberRepository.findByUserAndChatRoom(user, chatRoom)
				.orElseThrow(() -> {
					log.warn("채팅방에 없는 사용자입니다: roomId={}, userId={}", roomId, userId);
					return new ChatMemberNotFoundException(userId, roomId);
				});

		ChatMessage chatMessage = chatMessageRepository.findByIdAndChatRoomAndSender(messageId, chatRoom, sender)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 메시지입니다: messageId={}, roomId={}", messageId, roomId);
					return new ChatMessageNotFoundException(messageId, roomId);
				});

		if (!chatMessage.getSender().getUser().getId().equals(userId)) {
			throw new InvalidAccessException("메시지를 삭제할 권한이 없습니다");
		}

		chatMessage.delete();

		chatEventHandler.handleMessageDelete(ChatMessageDeleteEvent.of(chatMessage, chatRoom, userId));
		log.info("메시지를 삭제하였습니다: userId={}, roomId={}", userId, roomId);
	}

}
