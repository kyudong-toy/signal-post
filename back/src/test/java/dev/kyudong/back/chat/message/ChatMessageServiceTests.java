package dev.kyudong.back.chat.message;

import dev.kyudong.back.chat.api.dto.event.ChatMessageCreateEvent;
import dev.kyudong.back.chat.api.dto.event.ChatMessageDeleteEvent;
import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatMessage;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.MessageType;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.exception.ChatMessageNotFoundException;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.chat.repository.ChatMessageRepository;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.chat.service.ChatMessageService;
import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private ChatMemberRepository chatMemberRepository;

	@Mock
	private ChatEventHandler chatEventHandler;

	@InjectMocks
	private ChatMessageService chatMessageService;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static ChatRoom makeMockChatRoom(List<User> userList) {
		ChatRoom mockChatRoom = ChatRoom.builder()
				.initUserList(userList)
				.build();
		ReflectionTestUtils.setField(mockChatRoom, "id", 1L);
		return mockChatRoom;
	}

	private static ChatMember makeMockChatMember(User user) {
		ChatMember mockChatMember = ChatMember.builder()
				.user(user)
				.build();
		ReflectionTestUtils.setField(mockChatMember, "id", 1L);
		return mockChatMember;
	}

	private static ChatMessage makeMockChatMessage(ChatMember sender, Long id) {
		ChatMessage newChatMessage = ChatMessage.builder()
				.sender(sender)
				.content("본문")
				.messageType(MessageType.TEXT)
				.build();
		ReflectionTestUtils.setField(newChatMessage, "id", id);
		ReflectionTestUtils.setField(newChatMessage, "createdAt", Instant.now());
		return newChatMessage;
	}

	@Test
	@DisplayName("채팅방 참여 - 성공")
	void inviteChatroom_success() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		User mockUser2 = makeMockUser("ekcnvkzx", 2L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1, mockUser2));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		ChatMember mockChatMember = makeMockChatMember(mockUser1);
		given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
				.willReturn(Optional.of(mockChatMember));

		ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
		given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(mockChatMessage);

		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);
		doNothing().when(chatEventHandler).handleMessageCreate(any(ChatMessageCreateEvent.class));

		// when
		chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request);

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		then(chatMessageRepository).should().save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("채팅 메시지 생성 - 실패 : 채팅방이 존재하지 않음")
	void createMessage_fail_chatRoomNotFound() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.empty());

		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);

		// when
		assertThatThrownBy(() -> chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request))
				.isInstanceOf(ChatRoomNotFoundException.class);

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should(never()).findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		then(chatMessageRepository).should(never()).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("채팅 메시지 생성 - 실패 : 채팅 사용자가 방에 존재하지 않음")
	void createMessage_fail_memberNotFound() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		given(chatMemberRepository.findByUserAndChatRoom(mockUser1, mockChatRoom))
				.willReturn(Optional.empty());

		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);

		// when
		assertThatThrownBy(() -> chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request))
				.isInstanceOf(ChatMemberNotFoundException.class);

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		then(chatMessageRepository).should(never()).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("채팅 메시지 삭제 - 성공")
	void deleteMessage_success() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		User mockUser2 = makeMockUser("ekcnvkzx", 2L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1, mockUser2));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		ChatMember mockChatMember = makeMockChatMember(mockUser1);
		given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
				.willReturn(Optional.of(mockChatMember));

		ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
		ReflectionTestUtils.setField(mockChatMessage, "chatRoom", mockChatRoom);
		given(chatMessageRepository
				.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class)))
				.willReturn(Optional.of(mockChatMessage));
		doNothing().when(chatEventHandler).handleMessageDelete(any(ChatMessageDeleteEvent.class));

		// when
		chatMessageService.deleteMessage(mockUser1.getId(), mockChatRoom.getId(), mockChatMessage.getId());

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		then(chatMessageRepository).should()
				.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class));
	}

	@Test
	@DisplayName("채팅 메시지 삭제 - 실패 : 채팅방에 존재하지 메시지")
	void deleteMessage_fail_chatMessageNotFound() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		ChatMember mockChatMember = makeMockChatMember(mockUser1);
		given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
				.willReturn(Optional.of(mockChatMember));

		given(chatMessageRepository.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class)))
				.willThrow(ChatMessageNotFoundException.class);

		// when
		assertThatThrownBy(() -> chatMessageService.deleteMessage(mockUser1.getId(), mockChatRoom.getId(), 999L))
				.isInstanceOf(ChatMessageNotFoundException.class);

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
	}

	@Test
	@DisplayName("채팅 메시지 삭제 - 실패 : 메시지 삭제 권한 없음")
	void deleteMessage_fail_invalidAccess() {
		// given
		User mockUser1 = makeMockUser("zdfzdsf", 1L);
		User mockUser2 = makeMockUser("ekcnvkzx", 2L);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = makeMockChatRoom(List.of(mockUser1, mockUser2));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		ChatMember mockChatMember = makeMockChatMember(mockUser1);
		given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
				.willReturn(Optional.of(mockChatMember));

		ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
		ReflectionTestUtils.setField(mockChatMessage, "chatRoom", mockChatRoom);
		given(chatMessageRepository
				.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class)))
				.willReturn(Optional.of(mockChatMessage));

		// when
		assertThatThrownBy(() -> chatMessageService.deleteMessage(mockUser2.getId(), mockChatRoom.getId(), mockChatMessage.getId()))
				.isInstanceOf(InvalidAccessException.class);

		// then
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
	}

	@Test
	@DisplayName("채팅 메시지 조회 - 성공")
	void findChatMessage_success() {
		// given
		final Long roomId = 1L;
		User mockUser = makeMockUser("axzczxcdn", 1L);
		ChatMember mockChatMember = makeMockChatMember(mockUser);
		ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
		Slice<ChatMessage> chatMessages = new SliceImpl<>(List.of(mockChatMessage));
		given(chatMessageRepository.findChatMessage(anyLong(), any(PageRequest.class)))
				.willReturn(chatMessages);

		// when
		ChatMessageResDto response = chatMessageService.findMessages(roomId, null, null);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isFalse();
		assertThat(response.cursorId()).isNull();
		assertThat(response.cursorTime()).isNull();
		then(chatMessageRepository)
				.should().findChatMessage(anyLong(), any(PageRequest.class));
		then(chatMessageRepository).should(never())
				.findChatMessageByCursorTime(anyLong(), anyLong(), any(Instant.class), any(PageRequest.class));
	}

	@Test
	@DisplayName("채팅 메시지 조회 - 성공 : 커서를 이용")
	void findChatMessage_success_withCursor() {
		// given
		final Long roomId = 1L;
		User mockUser = makeMockUser("axzczxcdn", 1L);
		ChatMember mockChatMember = makeMockChatMember(mockUser);
		ChatMessage mockChatMessage1 = makeMockChatMessage(mockChatMember, 1L);
		ChatMessage mockChatMessage2 = makeMockChatMessage(mockChatMember, 2L);
		ChatMessage mockChatMessage3 = makeMockChatMessage(mockChatMember, 3L);
		Slice<ChatMessage> chatMessages = new SliceImpl<>(
				List.of(mockChatMessage1, mockChatMessage2, mockChatMessage3),
				PageRequest.of(0, 10),
				true
		);
		given(chatMessageRepository.findChatMessage(anyLong(), any(PageRequest.class)))
				.willReturn(chatMessages);

		// when
		ChatMessageResDto response = chatMessageService.findMessages(roomId, null, null);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isTrue();
		assertThat(response.cursorId()).isNotNull();
		assertThat(response.cursorTime()).isNotNull();
		then(chatMessageRepository)
				.should().findChatMessage(anyLong(), any(PageRequest.class));
		then(chatMessageRepository).should(never())
				.findChatMessageByCursorTime(anyLong(), anyLong(), any(Instant.class), any(PageRequest.class));
	}

}
