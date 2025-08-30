package dev.kyudong.back.chat.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.dto.event.ChatMessageCreateEvent;
import dev.kyudong.back.chat.api.dto.event.ChatMessageDeleteEvent;
import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
import dev.kyudong.back.chat.domain.*;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.repository.ChatMessageRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.then;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ChatMessageIntegrationTests {

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

	@Autowired
	private ChatMessageRepository chatMessageRepository;

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

	private void createTestChatMessages(List<ChatMessage> chatMessageList) {
		chatMessageRepository.saveAll(chatMessageList);
	}

	@Test
	@DisplayName("채팅 메시지 목록 조회")
	void findMessages() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("zzqqcvvv");
		List<User> userList = List.of(user1, user2);

		ChatRoom chatRoom = createTestChatRoom(userList);
		ChatMember member1 = chatMemberRepository.findByUserAndChatRoom(user1, chatRoom).orElseThrow();
		ChatMember member2 = chatMemberRepository.findByUserAndChatRoom(user2, chatRoom).orElseThrow();

		List<ChatMember> members = List.of(member1, member2);
		Random random = new Random();

		List<ChatMessage> chatMessageList = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			// 발신자 랜덤으로 진행
			ChatMember randomSender = members.get(random.nextInt(members.size()));

			ChatMessage chatMessage = ChatMessage.builder()
					.sender(randomSender)
					.content(randomSender.getUser().getUsername() + "가 보냄, 메시지 : " + i)
					.messageType(MessageType.TEXT)
					.build();
			chatMessageList.add(chatMessage);
			chatRoom.addMessage(chatMessage);
		}
		createTestChatMessages(chatMessageList);

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/chatroom/{roomId}/message", chatRoom.getId())
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user1)))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String body = result.getResponse().getContentAsString();
		ChatMessageResDto response = objectMapper.readValue(body, ChatMessageResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isTrue();
	}

	@Test
	@DisplayName("채팅 메시지 목록 조회 - 커서로 조회")
	void findMessages_withCurosr() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("zzqqcvvv");
		List<User> userList = List.of(user1, user2);

		ChatRoom chatRoom = createTestChatRoom(userList);
		ChatMember member1 = chatMemberRepository.findByUserAndChatRoom(user1, chatRoom).orElseThrow();
		ChatMember member2 = chatMemberRepository.findByUserAndChatRoom(user2, chatRoom).orElseThrow();

		List<ChatMember> members = List.of(member1, member2);
		Random random = new Random();

		List<ChatMessage> chatMessageList = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			// 발신자 랜덤으로 진행
			ChatMember randomSender = members.get(random.nextInt(members.size()));

			ChatMessage chatMessage = ChatMessage.builder()
					.sender(randomSender)
					.content(randomSender.getUser().getUsername() + "가 보냄, 메시지 : " + i)
					.messageType(MessageType.TEXT)
					.build();
			chatMessageList.add(chatMessage);
			chatRoom.addMessage(chatMessage);
		}
		createTestChatMessages(chatMessageList);

		final Long cursorId = chatMessageList.get(10).getId();
		final Instant cursorTime = chatMessageList.get(10).getCreatedAt();

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/chatroom/{roomId}/message", chatRoom.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user1))
						.param("cursorId", String.valueOf(cursorId))
						.param("cursorTime", String.valueOf(cursorTime)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		String body = result.getResponse().getContentAsString();
		ChatMessageResDto response = objectMapper.readValue(body, ChatMessageResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isTrue();
	}

	@Test
	@DisplayName("채팅 메시지 생성")
	void createMessage() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("dfsxvzzc");
		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto(
				"메시지 받아라!",
				MessageType.TEXT
		);
		ChatRoom chatRoom = createTestChatRoom(List.of(user1, user2));

		// when
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/message", chatRoom.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user1))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		then(chatEventHandler).should(timeout(1000).times(1)).handleMessageCreate(any(ChatMessageCreateEvent.class));
	}

	@Test
	@DisplayName("채팅 메시지 삭제")
	void deleteMessage() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("dfsxvzzc");
		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto(
				"메시지 받아라!",
				MessageType.TEXT
		);
		ChatRoom chatRoom = createTestChatRoom(List.of(user1, user2));

		ChatMember sender = chatMemberRepository.findByUserAndChatRoom(user1, chatRoom).orElseThrow();
		ChatMessage newChatMessage = ChatMessage.builder()
				.sender(sender)
				.content("메시지 본문임다")
				.messageType(MessageType.TEXT)
				.build();
		chatRoom.addMessage(newChatMessage);
		ChatMessage savedChatMessage = chatMessageRepository.save(newChatMessage);

		// when
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/message/{messageId}", chatRoom.getId(), savedChatMessage.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user1))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent())
				.andDo(print())
				.andReturn();

		// then
		then(chatEventHandler).should(timeout(1000).times(1)).handleMessageDelete(any(ChatMessageDeleteEvent.class));
		Optional<ChatMessage> optionalChatMessage = chatMessageRepository.findById(savedChatMessage.getId());
		assertThat(optionalChatMessage).isPresent();
		assertThat(optionalChatMessage.get().getMessageStatus()).isEqualTo(MessageStatus.DELETED);
	}


}
