package dev.kyudong.back.chat.member;

import dev.kyudong.back.chat.api.dto.event.ChatMemberInviteEvent;
import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.MemberStatus;
import dev.kyudong.back.chat.domain.RoomStatus;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.chat.service.ChatMemberService;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

public class ChatMemberServiceTests extends UnitTestBase {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatMemberRepository chatMemberRepository;

	@InjectMocks
	private ChatMemberService chatMemberService;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Nested
	@DisplayName("채팅방 초대")
	class InviteChatroom {

		@Test
		@DisplayName("채팅방 초대 - 성공")
		void inviteChatroom_succes() {
			// given
			User mockUser = createMockUser("mockUser", 1L);
			ChatRoom chatRoom = createMockChatRoom(List.of(mockUser));
			given(chatRoomRepository.findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class)))
					.willReturn(Optional.of(chatRoom));

			Set<Long> existsUserIds = Set.of(mockUser.getId());
			given(chatMemberRepository.findExistsMemberUserIds(
					anySet(), any(ChatRoom.class), any(MemberStatus.class)
			)).willReturn(existsUserIds);

			User inviteUser = createMockUser("mockUser2", 2L);
			given(userRepository.findByIdIn(anySet())).willReturn(List.of(inviteUser));

			ChatMemberInviteReqDto requset = new ChatMemberInviteReqDto(Set.of(2L));
			doNothing().when(applicationEventPublisher).publishEvent(any(ChatMemberInviteEvent.class));

			// when
			chatMemberService.inviteChatRoom(mockUser.getId(), chatRoom.getId(), requset);

			// then
			then(chatRoomRepository).should().findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class));
			then(chatMemberRepository).should().findExistsMemberUserIds(anySet(), any(ChatRoom.class), any(MemberStatus.class));
		}

		@Test
		@DisplayName("채팅방 초대 - 실패 : 채팅방에 없는 사용자가 초대")
		void inviteChatroom_fail_memberNotFound() {
			// given
			User mockUser = createMockUser("mockkUser", 1L);
			ChatRoom chatRoom = createMockChatRoom(List.of(mockUser));
			given(chatRoomRepository.findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class)))
					.willReturn(Optional.of(chatRoom));

			Set<Long> existsUserIds = Set.of(9999L);
			given(chatMemberRepository.findExistsMemberUserIds(
					anySet(), any(ChatRoom.class), any(MemberStatus.class)
			)).willReturn(existsUserIds);

			ChatMemberInviteReqDto requset = new ChatMemberInviteReqDto(Set.of(2L));

			// when
			assertThatThrownBy(() -> chatMemberService.inviteChatRoom(mockUser.getId(), chatRoom.getId(), requset))
					.isInstanceOf(ChatMemberNotFoundException.class);

			// then
			then(chatRoomRepository).should().findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class));
			then(chatMemberRepository).should().findExistsMemberUserIds(anySet(), any(ChatRoom.class), any(MemberStatus.class));
		}
		
	}

	@Nested
	@DisplayName("채팅방 탈퇴")
	class LeaveChatroom {

		@Test
		@DisplayName("성공")
		void succes() {
			// given
			User mockUser = createMockUser("mockUser", 1L);
			ChatRoom chatRoom = createMockChatRoom(List.of(mockUser));
			given(chatRoomRepository.findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class)))
					.willReturn(Optional.of(chatRoom));

			final Long leaveUserId = mockUser.getId();
			given(userRepository.getReferenceById(eq(leaveUserId))).willReturn(mockUser);

			ChatMember mockMember = ChatMember.builder()
					.user(mockUser)
					.chatRoom(chatRoom)
					.build();
			given(chatMemberRepository.findByUserAndChatRoom(any(User.class), any(ChatRoom.class)))
					.willReturn(Optional.of(mockMember));

			// when
			chatMemberService.leaveChatRoom(chatRoom.getId(), mockUser.getId());

			// then
			then(chatRoomRepository).should().findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class));
			then(chatMemberRepository).should().findByUserAndChatRoom(any(User.class), any(ChatRoom.class));
			assertThat(mockMember.getStatus()).isEqualTo(MemberStatus.LEFT);
		}

		@Test
		@DisplayName("실패 : 채팅방에 없는 사용자가 초대")
		void fail_memberNotFound() {
			// given
			User mockUser = createMockUser("mockUser", 1L);
			ChatRoom chatRoom = createMockChatRoom(List.of(mockUser));
			given(chatRoomRepository.findChatroomByIdAndStatus(anyLong(), any(RoomStatus.class)))
					.willReturn(Optional.of(chatRoom));

			given(chatMemberRepository.findExistsMemberUserIds(
					anySet(), any(ChatRoom.class), any(MemberStatus.class)
			)).willThrow(ChatMemberNotFoundException.class);

			ChatMemberInviteReqDto requset = new ChatMemberInviteReqDto(Set.of(2L));

			// when & then
			assertThatThrownBy(() -> chatMemberService.inviteChatRoom(mockUser.getId(), chatRoom.getId(), requset))
					.isInstanceOf(ChatMemberNotFoundException.class);
		}

	}

}
