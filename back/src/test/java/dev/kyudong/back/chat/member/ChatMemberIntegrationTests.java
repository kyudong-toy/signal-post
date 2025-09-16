package dev.kyudong.back.chat.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
import dev.kyudong.back.chat.domain.*;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ChatMemberIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private ChatMemberRepository chatMemberRepository;

	/**
	 * 웹소켓 이벤트는 해당 테스트 클래스에서 다루지 않습니다
	 */
	@SuppressWarnings("unused")
	@MockitoSpyBean
	private ChatEventHandler chatEventHandler;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser(String username) {
		User newUser = User.builder()
				.username(username)
				.rawPassword("password")
				.encodedPassword("password")
				.build();
		return userRepository.save(newUser);
	}

	private ChatRoom createTestChatRoom(List<User> userList) {
		ChatRoom newChatRoom = ChatRoom.builder()
				.initUserList(userList)
				.build();
		return chatRoomRepository.save(newChatRoom);
	}

	@Test
	@DisplayName("채팅방 초대")
	void inviteChatRoom() throws Exception {
		// given
		User user = createTestUser("dkdlsa");
		ChatRoom chatRoom = createTestChatRoom(List.of(user));

		// 초대할 사용자
		User inviteUser = createTestUser("ckvczxvcxz");

		ChatMemberInviteReqDto request = new ChatMemberInviteReqDto(Set.of(user.getId(), inviteUser.getId()));

		// when
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/members", chatRoom.getId())
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNoContent())
					.andDo(print())
					.andReturn();

		// then
		Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserAndChatRoom(inviteUser, chatRoom);
		assertThat(optionalChatMember).isPresent();

		ChatMember chatMember = optionalChatMember.get();
		assertThat(chatMember.getChatRoom()).isEqualTo(chatRoom);
		assertThat(chatMember.getUser()).isEqualTo(inviteUser);
		assertThat(chatMember.getStatus()).isEqualTo(MemberStatus.JOINED);
	}

	@Test
	@DisplayName("채팅방 탈퇴")
	void leaveChatRoom() throws Exception {
		// given
		User user = createTestUser("dkdlsa");
		ChatRoom chatRoom = createTestChatRoom(List.of(user));

		// when
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/members/me", chatRoom.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		Optional<ChatMember> optionalChatMember = chatMemberRepository.findByUserAndChatRoom(user, chatRoom);
		assertThat(optionalChatMember).isPresent();

		ChatMember chatMember = optionalChatMember.get();
		assertThat(chatMember.getChatRoom()).isEqualTo(chatRoom);
		assertThat(chatMember.getUser()).isEqualTo(user);
		assertThat(chatMember.getStatus()).isEqualTo(MemberStatus.LEFT);
	}

}
