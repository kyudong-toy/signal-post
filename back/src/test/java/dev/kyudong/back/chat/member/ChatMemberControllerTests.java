package dev.kyudong.back.chat.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.api.ChatMemberController;
import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.chat.service.ChatMemberService;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatMemberController.class)
@Import(SecurityConfig.class)
public class ChatMemberControllerTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private ChatMemberService chatMemberService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("채팅방 초대 - 성공")
	@WithMockCustomUser(id = 999L)
	void inviteChatroomApi_success() throws Exception {
		// given
		final Long userId = 999L;
		final Long roomId = 1L;
		ChatMemberInviteReqDto requset = new ChatMemberInviteReqDto(Set.of(1L));
		doNothing().when(chatMemberService).inviteChatRoom(userId, roomId, requset);

		// when & then
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/members", roomId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(requset)))
				.andExpect(status().isNoContent())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅방 초대 - 실패 : 존재하지 않는 채팅방")
	@WithMockCustomUser(id = 999L)
	void inviteChatroomApi_fail_roomNotFound() throws Exception {
		// given
		final Long roomId = 1L;
		ChatMemberInviteReqDto requset = new ChatMemberInviteReqDto(Set.of(1L));
		willThrow(new ChatRoomNotFoundException(roomId))
				.given(chatMemberService)
				.inviteChatRoom(anyLong(), anyLong(), any(ChatMemberInviteReqDto.class));

		// when & then
		mockMvc.perform(post("/api/v1/chatroom/{roomId}/members", roomId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(requset)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅방 탈퇴 - 성공")
	@WithMockCustomUser(id = 999L)
	void leaveChatroomApi_success() throws Exception {
		// given
		final Long userId = 999L;
		final Long roomId = 1L;
		doNothing().when(chatMemberService).leaveChatRoom(userId, roomId);

		// when & then
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/members/me", roomId))
				.andExpect(status().isNoContent())
				.andDo(print());
	}

	@Test
	@DisplayName("채팅방 탈퇴 - 실패 : 채팅방에 없는 사용자")
	@WithMockCustomUser(id = 999L)
	void leaveChatroomApi_fail_memberNotFound() throws Exception {
		// given
		final Long leaveUserId = 999L;
		final Long roomId = 1L;
		willThrow(new ChatMemberNotFoundException(leaveUserId, roomId))
				.given(chatMemberService)
				.leaveChatRoom(anyLong(), anyLong());

		// when & then
		mockMvc.perform(delete("/api/v1/chatroom/{roomId}/members/me", roomId))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

}
