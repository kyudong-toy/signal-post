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
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UsersNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatEventHandler chatEventHandler;

	@InjectMocks
	private ChatRoomService chatRoomService;

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
		ReflectionTestUtils.setField(mockChatRoom, "createdAt", Instant.now());
		return mockChatRoom;
	}

	@Test
	@DisplayName("채팅방 조회 - 성공")
	void findChatrooms_succes() {
		// given
		Long userId = 1L;
		User mockUser = makeMockUser("dkxxz", userId);
		given(userRepository.getReferenceById(anyLong())).willReturn(mockUser);

		ChatRoom chatRoom1 = makeMockChatRoom(
				List.of(
						makeMockUser("zdsfzd", 9L)
				)
		);
		ChatRoom chatRoom2 = makeMockChatRoom(
				List.of(
						makeMockUser("zzzz1dfbbv", 83L),
						makeMockUser("z1fdvfv", 1283L),
						makeMockUser("zzzz", 1111L)
				)
		);

		PageRequest pageRequest = PageRequest.of(0, 10);
		Slice<ChatRoom> chatrooms = new SliceImpl<>(List.of(chatRoom1, chatRoom2), pageRequest, false);
		given(chatRoomRepository.findChatRooomsByMember(mockUser, pageRequest))
				.willReturn(chatrooms);

		// when
		ChatRoomResDto response = chatRoomService.findChatRooms(userId, null, null);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isFalse();
		then(userRepository).should().getReferenceById(anyLong());
		then(chatRoomRepository).should().findChatRooomsByMember(any(User.class), any(PageRequest.class));
	}

	@Test
	@DisplayName("채팅룸 생성 - 성공")
	void createChatRoom_success() {
		// given
		User mockUser1 = makeMockUser("cnzn1d", 1L);
		User mockUser2 = makeMockUser("zzsda", 2L);

		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto("hello", Set.of(1L, 2L));

		List<User> userList = List.of(mockUser1, mockUser2);
		given(userRepository.findByIdIn(request.userIds())).willReturn(userList);

		ChatRoom chatRoom = makeMockChatRoom(userList);
		given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

		doNothing().when(chatEventHandler).handleRoomCreate(any(ChatRoomCreateEvent.class));

		// when
		ChatRoomCreateResDto response = chatRoomService.createChatRoom(mockUser1.getId(), request);

		// then
		assertThat(response.memberCount()).isEqualTo(2);
		assertThat(response.status()).isEqualTo(RoomStatus.ACTIVE);
		then(userRepository).should().findByIdIn(request.userIds());
		then(chatRoomRepository).should().save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("채팅룸 생성 - 실패 : 채팅방 사용자들 조회가 안됨")
	void createChatRoom_fail_usersNotFound() {
		// given
		User mockUser = makeMockUser("cnzn1d", 1L);

		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto("hello", Set.of(1L, 2L));

		given(userRepository.findByIdIn(Set.of(1L, 2L))).willReturn(new ArrayList<>());

		// when & then
		assertThatThrownBy(() -> chatRoomService.createChatRoom(mockUser.getId(), request))
				.isInstanceOf(UsersNotFoundException.class);
	}

}
