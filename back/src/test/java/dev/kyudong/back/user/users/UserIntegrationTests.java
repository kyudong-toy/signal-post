package dev.kyudong.back.user.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserPasswordUpdateReqDto;
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

	private User createTestUser() {
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		return userRepository.save(newUser);
	}

	@Test
	@DisplayName("사용자 조회 API - 게스트")
	void findUser_withGuest() throws Exception {
		// given
		User user = createTestUser();
		final String testUsername = user.getUsername();

		// when & then
		mockMvc.perform(get("/api/v1/users/{username}", testUsername))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.userId").value(user.getId()))
					.andExpect(jsonPath("$.username").value(user.getUsername()))
					.andDo(print());
	}

	@Test
	@DisplayName("사용자 조회 API - 자기자신 조회")
	void findUser_withUser() throws Exception {
		// given
		User user = createTestUser();
		final String testUsername = user.getUsername();

		// when & then
		mockMvc.perform(get("/api/v1/users/{username}", testUsername)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(user.getId()))
				.andExpect(jsonPath("$.username").value(user.getUsername()))
				.andExpect(jsonPath("$.isOwner").isBoolean())
				.andDo(print());
	}

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
				.andExpect(jsonPath("$.role").value(UserRole.USER.name()))
				.andDo(print());

		// then
		User foundUser = userRepository.findByUsername(testUsername).orElse(null);
		assertThat(foundUser).isNotNull();
		assertThat(foundUser.getRole()).isEqualTo(UserRole.USER);
		assertThat(passwordEncoder.matches(request.password(), foundUser.getPassword())).isTrue();
	}

	@Test
	@DisplayName("사용자 프로필 수정 API")
	void updateProfile() throws Exception {
		// given
		User user = createTestUser();
		UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newPassword");

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/update")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.username").value(user.getUsername()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 비밀번호 수정 API")
	void updatePassword() throws Exception {
		// given
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		User savedUser = userRepository.save(newUser);
		UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newPassword");

		// when & then
		mockMvc.perform(patch("/api/v1/users/me/password/update")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(savedUser))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.username").value(savedUser.getUsername()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
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
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(savedUser))
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

}
