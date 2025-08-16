package dev.kyudong.back.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.user.api.UserController;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.UserCreateResDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserStatusUpdateResDto;
import dev.kyudong.back.user.api.dto.res.UserUpdateResDto;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(UserController.class)
public class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserService userService;

	@Test
	@DisplayName("사용자 생성 API - 성공")
	void createUserApi_success() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("userName", "passWord");
		UserCreateResDto response = new UserCreateResDto(1L, "userName");
		when(userService.createUser(any(UserCreateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.userName").value("userName"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 생성 API - 실패 : 이미 존재하는 userName")
	void createUserApi_fail() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("userName", "passWord");
		when(userService.createUser(any(UserCreateReqDto.class)))
				.thenThrow(new UserAlreadyExistsException(request.userName() + " Already Exists"));

		// when & then
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Duplicate User"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value( request.userName() + " Already Exists"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 수정 API - 성공")
	void updateUserApi_success() throws Exception {
		// given
		long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto("passWord");
		UserUpdateResDto response = new UserUpdateResDto(userId, "userName", UserStatus.ACTIVE);
		when(userService.updateUser(eq(userId), any(UserUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.userName").value("userName"))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
	}

	@Test
	@DisplayName("사용자 수정 API - 실패 : 존재하지 않는 사용자")
	void updateUserApi_fail_userNotFound() throws Exception {
		// given
		long userId = 99L;
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");
		when(userService.updateUser(eq(userId), any(UserUpdateReqDto.class)))
				.thenThrow(new UserNotFoundException("User: {"+ userId + "} Not Found"));

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("User Not Found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("User: {"+ userId + "} Not Found"))
				.andDo(print());
	}

	// 사용자 패스워드 요청에 사용
	private static Stream<Arguments> provideInvalidPasswords() {
		return Stream.of(
				Arguments.of((String) null),       		// 1. null
				Arguments.of(""),          // 2. 빈 문자열 ""
				Arguments.of("    "),      // 3. 공백 문자 " "
				Arguments.of("z".repeat(151))    // 4. 151자 문자열 (동적 생성)
		);
	}

	@ParameterizedTest
	@DisplayName("사용자 수정 API - 실패 : 유효하지 않은 패스워드")
	@MethodSource("provideInvalidPasswords")
	void updateUser_fail_invalidPassword(String invalidPassword) throws Exception {
		// given
		long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto(invalidPassword);

		// 테스트를 위해 통일함
		when(userService.updateUser(eq(userId), any(UserUpdateReqDto.class)))
				.thenThrow(new InvalidInputException("Invalid Password"));

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid Input Value"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("Invalid Password"))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화 - 성공")
	void updateUserStatus_success() throws Exception {
		// given
		long userId = 1L; // url로 제공
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("passWord", UserStatus.ACTIVE);
		UserStatusUpdateResDto response = new UserStatusUpdateResDto(userId, UserStatus.ACTIVE);
		when(userService.updateUserStatus(eq(userId), any(UserStatusUpdateReqDto.class))).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}/status", userId)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인 - 성공")
	void loginUser_success() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");
		UserLoginResDto response = new UserLoginResDto(1L, "userName", UserStatus.ACTIVE);
		when(userService.loginUser(any(UserLoginReqDto.class))).thenReturn(response);

		// when & given
		mockMvc.perform(post("/api/v1/users/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.userName").value("userName"))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인 - 실패 : 비밀번호 불일치")
	void loginUser_fail_invalid_password() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");
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
