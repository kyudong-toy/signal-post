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
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
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

public class ChatMessageServiceTests extends UnitTestBase {

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

	@Mock
	private UserReaderService userReaderService;
	
	@Test
	@DisplayName("채팅방 참여 - 성공")
	void inviteChatroom_success() {
		// given
		User mockUser1 = createMockUser("zdfzdsf", 1L);
		User mockUser2 = createMockUser("ekcnvkzx", 2L);
		given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

		ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1, mockUser2));
		given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

		ChatMember mockChatMember = createMockChatMember(mockUser1);
		given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
				.willReturn(Optional.of(mockChatMember));

		ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
		given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(mockChatMessage);

		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);
		doNothing().when(chatEventHandler).handleMessageCreate(any(ChatMessageCreateEvent.class));

		// when
		chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request);

		// then
		then(userReaderService).should().getUserReference(anyLong());
		then(chatRoomRepository).should().findById(anyLong());
		then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		then(chatMessageRepository).should().save(any(ChatMessage.class));
	}

	@Nested
	@DisplayName("채팅 메시지 생성")
	class CreateMessage {

		@Test
		@DisplayName("채팅 메시지 생성 - 실패 : 채팅방이 존재하지 않음")
		void fail_chatRoomNotFound() {
			// given
			User mockUser1 = createMockUser("zdfzdsf", 1L);
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1));
			given(chatRoomRepository.findById(anyLong())).willReturn(Optional.empty());

			ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);

			// when
			assertThatThrownBy(() -> chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request))
					.isInstanceOf(ChatRoomNotFoundException.class);

			// then
			then(userReaderService).should().getUserReference(anyLong());
			then(chatRoomRepository).should().findById(anyLong());
			then(chatMemberRepository).should(never()).findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
			then(chatMessageRepository).should(never()).save(any(ChatMessage.class));
		}

		@Test
		@DisplayName("채팅 메시지 생성 - 실패 : 채팅 사용자가 방에 존재하지 않음")
		void fail_memberNotFound() {
			// given
			User mockUser1 = createMockUser("zdfzdsf", 1L);
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1));
			given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

			given(chatMemberRepository.findByUserAndChatRoom(mockUser1, mockChatRoom))
					.willReturn(Optional.empty());

			ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("본문", MessageType.TEXT);

			// when
			assertThatThrownBy(() -> chatMessageService.createMessage(mockUser1.getId(), mockChatRoom.getId(), request))
					.isInstanceOf(ChatMemberNotFoundException.class);

			// then
			then(userReaderService).should().getUserReference(anyLong());
			then(chatRoomRepository).should().findById(anyLong());
			then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
			then(chatMessageRepository).should(never()).save(any(ChatMessage.class));
		}

	}

	@Nested
	@DisplayName("치탱 메시지 삭제")
	class DeleteMessage {

		@Test
		@DisplayName("삭제 - 성공")
		void success() {
			// given
			User mockUser1 = createMockUser("zdfzdsf", 1L);
			User mockUser2 = createMockUser("ekcnvkzx", 2L);
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1, mockUser2));
			given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

			ChatMember mockChatMember = createMockChatMember(mockUser1);
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
			then(userReaderService).should().getUserReference(anyLong());
			then(chatRoomRepository).should().findById(anyLong());
			then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
			then(chatMessageRepository).should()
					.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class));
		}

		@Test
		@DisplayName("실패 : 채팅방에 존재하지 메시지")
		void fail_chatMessageNotFound() {
			// given
			User mockUser1 = createMockUser("zdfzdsf", 1L);
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1));
			given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

			ChatMember mockChatMember = createMockChatMember(mockUser1);
			given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
					.willReturn(Optional.of(mockChatMember));

			given(chatMessageRepository.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class)))
					.willThrow(ChatMessageNotFoundException.class);

			// when
			assertThatThrownBy(() -> chatMessageService.deleteMessage(mockUser1.getId(), mockChatRoom.getId(), 999L))
					.isInstanceOf(ChatMessageNotFoundException.class);

			// then
			then(userReaderService).should().getUserReference(anyLong());
			then(chatRoomRepository).should().findById(anyLong());
			then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		}

		@Test
		@DisplayName("실패 : 메시지 삭제 권한 없음")
		void fail_invalidAccess() {
			// given
			User mockUser1 = createMockUser("zdfzdsf", 1L);
			User mockUser2 = createMockUser("ekcnvkzx", 2L);
			given(userReaderService.getUserReference(anyLong())).willReturn(mockUser1);

			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser1, mockUser2));
			given(chatRoomRepository.findById(anyLong())).willReturn(Optional.of(mockChatRoom));

			ChatMember mockChatMember = createMockChatMember(mockUser1);
			given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
					.willReturn(Optional.of(mockChatMember));

			ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
			ReflectionTestUtils.setField(mockChatMessage, "chatRoom", mockChatRoom);
			given(chatMessageRepository
					.findByIdAndChatRoomAndSender(anyLong(), any(ChatRoom.class), any(ChatMember.class)))
					.willReturn(Optional.of(mockChatMessage));

			// when
			assertThatThrownBy(() -> chatMessageService.deleteMessage(mockUser2.getId(), mockChatRoom.getId(), mockChatMessage.getId()))
					.isInstanceOf(AccessDeniedException.class);

			// then
			then(userReaderService).should().getUserReference(anyLong());
			then(chatRoomRepository).should().findById(anyLong());
			then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
		}

	}

	@Nested
	@DisplayName("채팅 메시지 조회")
	class FindChatMessage {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			final Long roomId = 1L;
			User mockUser = createMockUser("axzczxcdn", 1L);
			ChatMember mockChatMember = createMockChatMember(mockUser);
			ChatMessage mockChatMessage = makeMockChatMessage(mockChatMember, 1L);
			Slice<ChatMessage> chatMessages = new SliceImpl<>(List.of(mockChatMessage));
			given(chatMessageRepository.findChatMessage(anyLong(), any(PageRequest.class))).willReturn(chatMessages);

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
		@DisplayName("성공 : 커서를 이용")
		void success_withCursor() {
			// given
			final Long roomId = 1L;
			User mockUser = createMockUser("axzczxcdn", 1L);
			ChatMember mockChatMember = createMockChatMember(mockUser);
			ChatRoom mockChatRoom = createMockChatRoom(List.of(mockUser));
			ChatMessage mockChatMessage1 = makeMockChatMessage(mockChatMember, 1L);
			ChatMessage mockChatMessage2 = makeMockChatMessage(mockChatMember, 2L);
			ChatMessage mockChatMessage3 = makeMockChatMessage(mockChatMember, 3L);
			Slice<ChatMessage> chatMessages = new SliceImpl<>(
					List.of(mockChatMessage1, mockChatMessage2, mockChatMessage3),
					PageRequest.of(0, 10),
					true
			);
			given(chatMessageRepository.findChatMessage(anyLong(), any(PageRequest.class))).willReturn(chatMessages);

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

}
