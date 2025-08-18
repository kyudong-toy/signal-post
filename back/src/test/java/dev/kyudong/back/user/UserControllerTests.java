package dev.kyudong.back.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.security.WithMockCustomUser;
import dev.kyudong.back.user.api.UserController;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.UserCreateResDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserStatusUpdateResDto;
import dev.kyudong.back.user.api.dto.res.UserUpdateResDto;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

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
				.andExpect(jsonPath("$.title").value("Duplicate User"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value( request.username() + " Already Exists"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 수정 API - 성공")
	@WithMockCustomUser
	void updateUserApi_success() throws Exception {
		// given
		final Long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto("password");
		UserUpdateResDto response = new UserUpdateResDto(userId, "username", UserStatus.ACTIVE);
		when(userService.updateUser(eq(userId), any(UserUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.username").value("username"))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
	}

	@Test
	@DisplayName("사용자 수정 API - 실패")
	@WithMockCustomUser(id = 999L)
	void updateUserApi_fail() throws Exception {
		// given
		final Long nonExistsUserId = 999L;
		UserUpdateReqDto request = new UserUpdateReqDto("newpassword");
		when(userService.updateUser(eq(nonExistsUserId), any(UserUpdateReqDto.class)))
				.thenThrow(new UserNotFoundException(nonExistsUserId));

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("User Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("User {"+ nonExistsUserId + "} Not Found"))
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
		when(userService.updateUserStatus(eq(userId), any(UserStatusUpdateReqDto.class))).thenReturn(response);

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
		when(userService.updateUserStatus(eq(userId), any(UserStatusUpdateReqDto.class)))
				.thenThrow(new UserNotFoundException(userId));

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/status", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("User Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("User {"+ userId + "} Not Found"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인 - 성공")
	void loginUserApi_success() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		UserLoginResDto response = new UserLoginResDto(1L, "username", "token");
		when(userService.loginUser(any(UserLoginReqDto.class))).thenReturn(response);

		// when & given
		mockMvc.perform(post("/api/v1/users/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("username"))
				.andExpect(jsonPath("$.token").isString())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인 - 실패")
	void loginUserApi_fail() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		when(userService.loginUser(any(UserLoginReqDto.class)))
				.thenThrow(new InvalidInputException("Password not Equals"));

		// when & given
		mockMvc.perform(post("/api/v1/users/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid Input Value"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("Password not Equals"))
				.andDo(print());
	}

}
