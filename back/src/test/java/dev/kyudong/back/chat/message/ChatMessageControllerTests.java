package dev.kyudong.back.chat.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.ChatMessageController;
import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageDetailResDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
import dev.kyudong.back.chat.domain.*;
import dev.kyudong.back.chat.exception.ChatMessageNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.chat.service.ChatMessageService;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatMessageController.class)
@Import(SecurityConfig.class)
public class ChatMessageControllerTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private ChatMessageService chatMessageService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("채팅 메시지 목록 조회 - 성공")
	@WithMockCustomUser(id = 999L)
	void findMessagesApi_success() throws Exception {
		// given
		final Long roomId = 1L;
		ChatMessageResDto response = new ChatMessageResDto(
				roomId,
				Instant.now(),
				false,
				List.of(
						new ChatMessageDetailResDto(
								1L, 
								1L,
								1L,
								"djfdkfd",
								"안녕",
								MessageType.TEXT,
								MessageStatus.ACTIVE,
								LocalDateTime.now()
						),
						new ChatMessageDetailResDto(
								2L,
								1L,
								2L,
								"cvxcaaz",
								"그래 안녕",
								MessageType.TEXT,
								MessageStatus.ACTIVE,
								LocalDateTime.now()
						),
						new ChatMessageDetailResDto(
								3L,
								1L,
								1L,
								"djfdkfd",
								"뭘봐",
								MessageType.TEXT,
								MessageStatus.ACTIVE,
								LocalDateTime.now()
						)
				)
		);
		given(chatMessageService.findMessages(roomId, null, null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/chatroom/{roomId}/message", roomId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cursorId").value(roomId))
				.andExpect(jsonPath("$.hasNext").value(false))
				.andExpect(jsonPath("$.content").isArray())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅 메시지 생성 - 성공")
	@WithMockCustomUser(id = 999L)
	void createMessageApi_success() throws Exception {
		// given
		final Long userId = 999L;
		final Long roomId = 999L;
		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("아아아아아아아ㅏ아앙", MessageType.TEXT);
		doNothing().when(chatMessageService).createMessage(userId, roomId, request);

		// when & then
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/message", roomId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅방 생성 - 실패 : 채팅방이 존재하지 않음")
	@WithMockCustomUser
	void createMessageApi_fail_roomNotFound() throws Exception {
		// given
		final Long roomId = 999L;
		ChatMessageCreateReqDto request = new ChatMessageCreateReqDto("아아아아아아아ㅏ아앙", MessageType.TEXT);

		willThrow(new ChatRoomNotFoundException(roomId))
				.given(chatMessageService)
				.createMessage(anyLong(), eq(roomId), any(ChatMessageCreateReqDto.class));

		// when & then
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/message", roomId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅 메시지 삭제 - 성공")
	@WithMockCustomUser(id = 999L)
	void deleteMessageApi_success() throws Exception {
		// given
		final Long userId = 999L;
		final Long roomId = 999L;
		final Long messageId = 999L;
		doNothing().when(chatMessageService).deleteMessage(userId, roomId, messageId);

		// when & then
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/message/{messageId}", roomId, messageId))
				.andExpect(status().isNoContent())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅방 생성 - 실패 : 삭제할 메시지가 없음")
	@WithMockCustomUser
	void deleteMessageApi_fail_messageNotFound() throws Exception {
		// given
		final Long roomId = 999L;
		final Long messageId = 999L;
		willThrow(new ChatMessageNotFoundException(messageId, roomId))
				.given(chatMessageService)
				.deleteMessage(anyLong(), eq(roomId), eq(messageId));

		// when & then
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/message/{messageId}", roomId, messageId))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

}
