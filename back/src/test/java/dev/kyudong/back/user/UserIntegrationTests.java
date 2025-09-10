package dev.kyudong.back.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UserIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Test
	@DisplayName("사용자 생성 API")
	void createUser() throws Exception {
		// given
		final String testUsername = "testuser";
		UserCreateReqDto request = new UserCreateReqDto(testUsername, "password");

		// when
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value(testUsername))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andExpect(jsonPath("$.role").value(UserRole.USER.name()));

		// then
		User foundUser = userRepository.findByUsername(testUsername).orElse(null);
		assertThat(foundUser).isNotNull();
		assertThat(foundUser.getRole()).isEqualTo(UserRole.USER);
		assertThat(passwordEncoder.matches(request.password(), foundUser.getPassword())).isTrue();
	}

	@Test
	@DisplayName("사용자 수정 API")
	void updateUser() throws Exception {
		// given
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		User savedUser = userRepository.save(newUser);
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(savedUser))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.username").value(savedUser.getUsername()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
	}

	@Test
	@DisplayName("사용자 상태 수정 API")
	void updateUserStatus() throws Exception {
		// given
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		User savedUser = userRepository.save(newUser);
		savedUser.dormantUser();
		userRepository.save(savedUser);
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(savedUser))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인")
	void loginUser() throws Exception {
		// given
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		User savedUser = userRepository.save(newUser);
		userRepository.save(savedUser);
		UserLoginReqDto request = new UserLoginReqDto("mockUser", "password");

		// when & then
		mockMvc.perform(post("/api/v1/users/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.username").value("mockUser"))
				.andExpect(jsonPath("$.token").isString())
				.andDo(print());
	}

}
