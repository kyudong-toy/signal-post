package dev.kyudong.back.chat.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.chat.domain.ChatRoom;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class ChatRoomIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

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

	@Test
	@DisplayName("채팅방 목록 조회")
	void findChatRooms() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("zzqqcvvv");
		ChatRoom newChatRoom = ChatRoom.builder()
				.initUserList(List.of(user1, user2))
				.build();
		chatRoomRepository.save(newChatRoom);

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/chatroom")
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user1)))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String body = result.getResponse().getContentAsString();
		ChatRoomResDto response = objectMapper.readValue(body, ChatRoomResDto.class);
		assertThat(response).isNotNull();
	}

	@Test
	@DisplayName("채팅방 생성")
	void createChatRoom() throws Exception {
		// given
		User user1 = createTestUser("dkdlsa");
		User user2 = createTestUser("dfsxvzzc");
		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto(
				"myRoom!",
				Set.of(user1.getId(), user2.getId())
		);

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/chatroom")
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user1))
								.contentType(MediaType.APPLICATION_JSON.toString())
								.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isCreated())
						.andExpect(header().exists("Location"))
						.andDo(print())
						.andReturn();

		// then
		String body = result.getResponse().getContentAsString();
		ChatRoomCreateResDto response = objectMapper.readValue(body, ChatRoomCreateResDto.class);
		assertThat(response).isNotNull();
	}

}
