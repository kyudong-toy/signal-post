package dev.kyudong.back.chat.ws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.dto.event.*;
import dev.kyudong.back.chat.api.dto.ws.*;
import dev.kyudong.back.chat.domain.*;
import dev.kyudong.back.chat.event.ChatEventHandler;
import dev.kyudong.back.chat.event.ChatEventType;
import dev.kyudong.back.chat.websocket.ChatWebSocketMessage;
import dev.kyudong.back.chat.repository.ChatMessageRepository;
import dev.kyudong.back.chat.repository.ChatMemberRepository;
import dev.kyudong.back.chat.repository.ChatRoomRepository;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChatWebSocketTests {

	@LocalServerPort
	private int port;

	@Autowired
	private ChatEventHandler chatEventHandler;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private ChatMemberRepository chatMemberRepository;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser() {
		User newUser = User.builder()
				.username(randomUsername())
				.rawPassword("password")
				.encodedPassword("password")
				.build();
		return userRepository.save(newUser);
	}

	private String randomUsername() {
		int length = ThreadLocalRandom.current().nextInt(20) + 4;
		StringBuilder username = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char randomChar = (char) ('a' + (ThreadLocalRandom.current().nextInt(26)));
			username.append(randomChar);
		}
		return username.toString();
	}

	private ChatRoom createTestChatRoom(List<User> userList) {
		ChatRoom newChatRoom = ChatRoom.builder()
				.initUserList(userList)
				.build();
		return chatRoomRepository.save(newChatRoom);
	}

	private ChatMessage createTestChatMessage(ChatMember sender, ChatRoom chatRoom) {
		ChatMessage newChatMessage = ChatMessage.builder()
				.sender(sender)
				.content("안녕하세요")
				.messageType(MessageType.TEXT)
				.build();
		chatRoom.addMessage(newChatMessage);
		return chatMessageRepository.save(newChatMessage);
	}

	private Set<ChatMember> createTestChatMembers(ChatRoom chatRoom, List<User> userList) {
		return chatRoom.addNewMember(userList);
	}

	private WebSocketSession connectWebSocketClient(String token, BlockingQueue<String> messageQueue) throws Exception {
		URI uri = new URI("ws://localhost:" + port + "/ws/chat?token=" + token);

		return new StandardWebSocketClient().execute(new TextWebSocketHandler() {
			@Override
			public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
				messageQueue.add(message.getPayload());
			}
		}, uri.toString()).get(1, TimeUnit.SECONDS);
	}

	@Test
	@DisplayName("채팅 메시지 생성 - 채팅바의 사용자에게 메시지를 전송한다")
	void handleMessageCreate() throws Exception {
		// given
		User userA = createTestUser();
		User userB = createTestUser();
		List<User> userList = List.of(userA, userB);
		ChatRoom chatRoom = createTestChatRoom(userList);

		BlockingQueue<String> messageQueueA = new LinkedBlockingQueue<>();
		BlockingQueue<String> messageQueueB = new LinkedBlockingQueue<>();
		WebSocketSession sessionA = connectWebSocketClient(jwtUtil.generateToken(userA), messageQueueA);
		WebSocketSession sessionB = connectWebSocketClient(jwtUtil.generateToken(userB), messageQueueB);

		// 이벤트 생성
		ChatMember sender = chatMemberRepository.findByUserAndChatRoom(userA, chatRoom).orElseThrow();
		ChatMessage chatMessage = createTestChatMessage(sender, chatRoom);
		ChatMessageCreateEvent payload = ChatMessageCreateEvent.of(chatMessage, chatRoom, userA);

		// when
		chatEventHandler.handleMessageCreate(payload);

		// then
		String messageA = messageQueueA.poll(1, TimeUnit.SECONDS);
		String messageB = messageQueueB.poll(1, TimeUnit.SECONDS);
		assertThat(messageA).isNotNull();
		assertThat(messageB).isNotNull();

		// 메시지 검증
		ChatWebSocketMessage<ChatNewMessageWsDto> receivedMessage = objectMapper.readValue(messageA, new TypeReference<>() {});
		assertThat(receivedMessage.payload().messageId()).isEqualTo(chatMessage.getId());
		assertThat(receivedMessage.payload().content()).isEqualTo(chatMessage.getContent());

		sessionA.close();
		sessionB.close();
	}

	@Test
	@DisplayName("채팅 메시지 삭제 - 채팅바의 사용자에게 삭제된 메시지를 전송한다")
	void handleMessageDelete() throws Exception {
		// given
		User userA = createTestUser();
		User userB = createTestUser();
		List<User> userList = List.of(userA, userB);
		ChatRoom chatRoom = createTestChatRoom(userList);

		BlockingQueue<String> messageQueueA = new LinkedBlockingQueue<>();
		BlockingQueue<String> messageQueueB = new LinkedBlockingQueue<>();
		WebSocketSession sessionA = connectWebSocketClient(jwtUtil.generateToken(userA), messageQueueA);
		WebSocketSession sessionB = connectWebSocketClient(jwtUtil.generateToken(userB), messageQueueB);

		// 이벤트 생성
		ChatMember sender = chatMemberRepository.findByUserAndChatRoom(userA, chatRoom).orElseThrow();
		ChatMessage chatMessage = createTestChatMessage(sender, chatRoom);
		chatMessage.delete();
		ChatMessageDeleteEvent payload = ChatMessageDeleteEvent.of(chatMessage, chatRoom, userA.getId());

		// when
		chatEventHandler.handleMessageDelete(payload);

		// then
		String messageA = messageQueueA.poll(1, TimeUnit.SECONDS);
		String messageB = messageQueueB.poll(1, TimeUnit.SECONDS);
		assertThat(messageA).isNotNull();
		assertThat(messageB).isNotNull();

		// 메시지 검증
		ChatWebSocketMessage<ChatDelMessageWsDto> receivedMessage = objectMapper.readValue(messageA, new TypeReference<>() {});
		assertThat(receivedMessage.payload().messageId()).isEqualTo(chatMessage.getId());
		assertThat(receivedMessage.payload().messageStatus()).isEqualTo(MessageStatus.DELETED);

		sessionA.close();
		sessionB.close();
	}

	@Test
	@DisplayName("채팅방 생성 - 채팅방을 생성하고 방에 대한 메시지를 전송한다")
	void handleRoomCreate() throws Exception {
		// given
		User userA = createTestUser();
		User userB = createTestUser();
		List<User> userList = List.of(userA, userB);
		ChatRoom chatRoom = createTestChatRoom(userList);

		BlockingQueue<String> messageQueueA = new LinkedBlockingQueue<>();
		BlockingQueue<String> messageQueueB = new LinkedBlockingQueue<>();
		WebSocketSession sessionA = connectWebSocketClient(jwtUtil.generateToken(userA), messageQueueA);
		WebSocketSession sessionB = connectWebSocketClient(jwtUtil.generateToken(userB), messageQueueB);

		// 이벤트 생성
		ChatRoomCreateEvent payload = ChatRoomCreateEvent.from(chatRoom);

		// when
		chatEventHandler.handleRoomCreate(payload);

		// then
		String messageA = messageQueueA.poll(1, TimeUnit.SECONDS);
		String messageB = messageQueueB.poll(1, TimeUnit.SECONDS);
		assertThat(messageA).isNotNull();
		assertThat(messageB).isNotNull();

		sessionA.close();
		sessionB.close();
	}

	@Test
	@DisplayName("채팅방 초대 - 채팅방에 사용자를 초대한다")
	void handleMemberInvite() throws Exception {
		// given
		User chatMember = createTestUser();
		User inviteUser = createTestUser();
		ChatRoom chatRoom = createTestChatRoom(List.of(chatMember));

		BlockingQueue<String> messageQueueA = new LinkedBlockingQueue<>();
		BlockingQueue<String> messageQueueB = new LinkedBlockingQueue<>();
		WebSocketSession sessionA = connectWebSocketClient(jwtUtil.generateToken(chatMember), messageQueueA);
		WebSocketSession sessionB = connectWebSocketClient(jwtUtil.generateToken(inviteUser), messageQueueB);

		// 이벤트 생성
		Set<ChatMember> newMembers = createTestChatMembers(
				chatRoom,
				List.of(
					inviteUser,
					createTestUser(),
					createTestUser(),
					createTestUser()
				)
		);
		ChatMemberInviteEvent event = ChatMemberInviteEvent.of(chatRoom, Set.of(chatMember.getId()), newMembers);

		// when
		chatEventHandler.handleMemberInvite(event);

		// then
		String messageA = messageQueueA.poll(1, TimeUnit.SECONDS);
		assertThat(messageA).isNotNull();
		ChatWebSocketMessage<ChatNewMemberWsDto> newMemberMessage = objectMapper.readValue(messageA, new TypeReference<>() {});
		assertThat(newMemberMessage.type()).isEqualTo(ChatEventType.NEW_MEMBER);

		String messageB = messageQueueB.poll(1, TimeUnit.SECONDS);
		assertThat(messageB).isNotNull();
		ChatWebSocketMessage<ChatMemberInviteWsDto> inviteMemberMessage = objectMapper.readValue(messageB, new TypeReference<>() {});
		assertThat(inviteMemberMessage.type()).isEqualTo(ChatEventType.INVITE_NEW_MEMBER);

		sessionA.close();
		sessionB.close();
	}

	@Test
	@DisplayName("채팅방 탈퇴 - 사용자가 채팅방을 탈퇴한다")
	void handleMemberLeft() throws Exception {
		// given
		User chatMember = createTestUser();
		User leaveUser = createTestUser();
		ChatRoom chatRoom = createTestChatRoom(List.of(chatMember, leaveUser));

		BlockingQueue<String> messageQueueA = new LinkedBlockingQueue<>();
		BlockingQueue<String> messageQueueB = new LinkedBlockingQueue<>();
		WebSocketSession sessionA = connectWebSocketClient(jwtUtil.generateToken(chatMember), messageQueueA);
		WebSocketSession sessionB = connectWebSocketClient(jwtUtil.generateToken(leaveUser), messageQueueB);

		// 이벤트 생성
		ChatMemberLeftEvent event = ChatMemberLeftEvent.of(chatRoom.getId(), leaveUser.getId(), chatRoom.getChatMembers());

		// when
		chatEventHandler.handleMemberLeft(event);

		// then
		String messageA = messageQueueA.poll(1, TimeUnit.SECONDS);
		assertThat(messageA).isNotNull();
		ChatWebSocketMessage<ChatMemberLeftWsDto> newMemberMessage = objectMapper.readValue(messageA, new TypeReference<>() {});
		assertThat(newMemberMessage.type()).isEqualTo(ChatEventType.LEFT_MEMBER);

		String messageB = messageQueueB.poll(1, TimeUnit.SECONDS);
		assertThat(messageB).isNull();

		sessionA.close();
		sessionB.close();
	}

}
