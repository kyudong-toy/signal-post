package dev.kyudong.back.chat.room;

import dev.kyudong.back.chat.api.dto.event.ChatRoomCreateEvent;
import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.RoomStatus;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.chat.service.ChatRoomService;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UsersNotFoundException;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

public class ChatRoomServiceTests extends UnitTestBase {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatEventHandler chatEventHandler;

	@InjectMocks
	private ChatRoomService chatRoomService;

	@Mock
	private UserReaderService userReaderService;

	@Test
	@DisplayName("채팅방 조회 - 성공")
	void findChatrooms_succes() {
		// given
		Long userId = 1L;
		User mockUser = createMockUser("dkxxz", userId);
		given(userReaderService.getUserReference(userId)).willReturn(mockUser);

		ChatRoom chatRoom1 = createMockChatRoom(
				List.of(
						createMockUser("zdsfzd", 9L)
				)
		);
		ChatRoom chatRoom2 = createMockChatRoom(
				List.of(
						createMockUser("zzzz1dfbbv", 83L),
						createMockUser("z1fdvfv", 1283L),
						createMockUser("zzzz", 1111L)
				)
		);

		PageRequest pageRequest = PageRequest.of(0, 10);
		Slice<ChatRoom> chatrooms = new SliceImpl<>(List.of(chatRoom1, chatRoom2), pageRequest, false);
		given(chatRoomRepository.findChatRooomsByMember(mockUser, pageRequest)).willReturn(chatrooms);

		// when
		ChatRoomResDto response = chatRoomService.findChatRooms(userId, null, null);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isFalse();
		then(userReaderService).should().getUserReference(userId);
		then(chatRoomRepository).should().findChatRooomsByMember(any(User.class), any(PageRequest.class));
	}

	@Test
	@DisplayName("채팅룸 생성 - 성공")
	void createChatRoom_success() {
		// given
		User mockUser1 = createMockUser("cnzn1d", 1L);
		User mockUser2 = createMockUser("zzsda", 2L);

		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto("hello", Set.of(1L, 2L));

		List<User> userList = List.of(mockUser1, mockUser2);
		given(userReaderService.getUsersByIds(request.userIds())).willReturn(userList);

		ChatRoom chatRoom = createMockChatRoom(userList);
		given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

		doNothing().when(chatEventHandler).handleRoomCreate(any(ChatRoomCreateEvent.class));

		// when
		ChatRoomCreateResDto response = chatRoomService.createChatRoom(mockUser1.getId(), request);

		// then
		assertThat(response.memberCount()).isEqualTo(2);
		assertThat(response.status()).isEqualTo(RoomStatus.ACTIVE);
		then(userReaderService).should().getUsersByIds(request.userIds());
		then(chatRoomRepository).should().save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("채팅룸 생성 - 실패 : 채팅방 사용자들 조회가 안됨")
	void createChatRoom_fail_usersNotFound() {
		// given
		User mockUser = createMockUser("cnzn1d", 1L);

		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto("hello", Set.of(1L, 2L));

		given(userReaderService.getUsersByIds(Set.of(1L, 2L))).willReturn(new ArrayList<>());

		// when & then
		assertThatThrownBy(() -> chatRoomService.createChatRoom(mockUser.getId(), request))
				.isInstanceOf(UsersNotFoundException.class);
	}

}
