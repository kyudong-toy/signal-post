package dev.kyudong.back.user.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
import dev.kyudong.back.user.api.UserController;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserProfileUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserPasswordUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.*;
import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserService userService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@Test
	@DisplayName("게스트가 사용자 조회 API - 성공")
	void findUserApi_success_withGuest() throws Exception {
		// given
		String username = "testUser";
		UserDetailResDto response = new UserDetailResDto(
				1L, username, "테스트사용자", "자기소개",
				null, "백그라운드이미지url", false
		);
		given(userService.findUser(username, null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/users/{username}", username))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1L))
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.isOwner").isBoolean())
				.andDo(print());
	}

	@Test
	@DisplayName("회원이 사용자 조회 API - 성공")
	@WithMockCustomUser
	void findUserApi_success_withUser() throws Exception {
		// given
		String username = "testUser";
		UserDetailResDto response = new UserDetailResDto(
				1L, username, "테스트사용자", "자기소개",
				null, "백그라운드이미지url", false
		);
		given(userService.findUser(username, 1L)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/users/{username}", username))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 조회 API - 실패")
	void findUserApi_fail() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("username", "password");
		UserCreateResDto response = new UserCreateResDto(1L, "username", UserStatus.ACTIVE, UserRole.USER);
		when(userService.createUser(any(UserCreateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("username"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 생성 API - 성공")
	void createUserApi_success() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("username", "password");
		UserCreateResDto response = new UserCreateResDto(1L, "username", UserStatus.ACTIVE, UserRole.USER);
		when(userService.createUser(any(UserCreateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("username"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 생성 API - 실패")
	void createUserApi_fail() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("username", "password");
		when(userService.createUser(any(UserCreateReqDto.class)))
				.thenThrow(new UserAlreadyExistsException(request.username()));

		// when & then
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Duplicate USER"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value( request.username() + " Already Exists"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 프로필 수정 API - 성공")
	@WithMockCustomUser
	void updateProfileApi_success() throws Exception {
		// given
		UserProfileUpdateReqDto request = new UserProfileUpdateReqDto(
				"테스트",
				"자기소개",
				null,
				null,
				null,
				null,
				null,
				null
		);

		UserProfileUpdateResDto response = new UserProfileUpdateResDto(
				1L,
				"test",
				"username",
				"자기소개",
				null,
				null,
				UserStatus.ACTIVE
		);
		given(userService.updateProfile(anyString(), any(UserProfileUpdateReqDto.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 프로필 수정 API - 실패")
	@WithMockCustomUser(id = 999L)
	void updateProfileApi_fail() throws Exception {
		// given
		final String username = "tester";
		UserProfileUpdateReqDto request = new UserProfileUpdateReqDto(
				"테스트",
				"자기소개",
				null,
				null,
				null,
				null,
				null,
				null
		);
		when(userService.updateProfile(anyString(), any(UserProfileUpdateReqDto.class)))
				.thenThrow(new UserNotFoundException(username));

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 비밀번호 수정 API - 성공")
	@WithMockCustomUser
	void updatePasswordApi_success() throws Exception {
		// given
		UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newPassword");

		UserPasswordUpdateResDto response = new UserPasswordUpdateResDto(1L, "username", UserStatus.ACTIVE);
		given(userService.updatePassword(anyString(), any(UserPasswordUpdateReqDto.class)))
				.willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/password/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 비밀번호 수정 API - 실패")
	@WithMockCustomUser(id = 999L)
	void updatePasswordApi_fail() throws Exception {
		// given
		final Long nonExistsUserId = 999L;
		UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newpassword");
		given(userService.updatePassword(anyString(), any(UserPasswordUpdateReqDto.class)))
				.willThrow(new UserNotFoundException(nonExistsUserId));

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/password/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 상태 변경 - 성공")
	@WithMockCustomUser
	void updateUserStatusApi_success() throws Exception {
		// given
		final Long userId = 1L;
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);
		UserStatusUpdateResDto response = new UserStatusUpdateResDto(userId, UserStatus.ACTIVE);
		given(userService.updateUserStatus(anyString(), any(UserStatusUpdateReqDto.class))).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/status")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 상태 변경 - 실패")
	@WithMockCustomUser
	void updateUserStatusApi_fail() throws Exception {
		// given
		final Long userId = 1L;
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);
		given(userService.updateUserStatus(anyString(), any(UserStatusUpdateReqDto.class)))
				.willThrow(new UserNotFoundException(userId));

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/status", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andDo(print());
	}

}
