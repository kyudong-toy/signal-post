package dev.kyudong.back.chat.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.ChatRoomController;
import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomDetailResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.chat.domain.RoomStatus;
import dev.kyudong.back.chat.service.ChatRoomService;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.security.WithMockCustomUser;
import dev.kyudong.back.user.exception.UsersNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRoomController.class)
@Import(SecurityConfig.class)
public class ChatRoomControllerTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private ChatRoomService chatRoomService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("채팅방 목록 조회 - 성공")
	@WithMockCustomUser(id = 999L)
	void findChatroomsApi_success() throws Exception {
		// given
		final Long userId = 999L;
		ChatRoomResDto response = new ChatRoomResDto(
				1L,
				Instant.now(),
				false,
				List.of(
						new ChatRoomDetailResDto(1L, 2),
						new ChatRoomDetailResDto(2L, 10),
						new ChatRoomDetailResDto(3L, 22)
				)
		);
		given(chatRoomService.findChatRooms(userId, null, null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/chatroom"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastChatroomId").value(1L))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andExpect(jsonPath("$.content").isArray())
				.andDo(print());
		then(chatRoomService).should().findChatRooms(userId, null, null);
	}

	@Test
	@DisplayName("채팅방 생성 - 성공")
	@WithMockCustomUser(id = 999L)
	void createChatRoomApi_success() throws Exception {
		// given
		final Long userId = 999L;
		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto(
				"myRoom!",
				Set.of(1L, 2L, 3L)
		);

		ChatRoomCreateResDto chatroomCreateResDto = new ChatRoomCreateResDto(
				1L, 3, RoomStatus.ACTIVE, LocalDateTime.now()
		);
		given(chatRoomService.createChatRoom(userId, request)).willReturn(chatroomCreateResDto);

		// when & then
		mockMvc.perform(post("/api/v1/chatroom")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.roomId").value(1L))
				.andExpect(jsonPath("$.memberCount").value(3))
				.andDo(print());
		then(chatRoomService).should().createChatRoom(userId, request);
	}

	@Test
	@DisplayName("채팅방 생성 - 실패 : 사용자들이 조회가 안됨")
	@WithMockCustomUser(id = 999L)
	void createChatRoomApi_fail_usersNotFound() throws Exception {
		// given
		final Long userId = 999L;
		ChatRoomCreateReqDto request = new ChatRoomCreateReqDto(
				"myRoom!",
				Set.of(1L, 2L, 3L)
		);

		given(chatRoomService.createChatRoom(userId, request)).willThrow(UsersNotFoundException.class);

		// when & then
		mockMvc.perform(post("/api/v1/chatroom")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

}
