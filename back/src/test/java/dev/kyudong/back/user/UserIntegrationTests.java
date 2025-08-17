package dev.kyudong.back.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자 생성 API")
	void createUser_integration_success() throws Exception {
		// given
		UserCreateReqDto request = new UserCreateReqDto("userName", "passWord");

		// when
		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userName").value("userName"));

		// then
		User foundUser = userRepository.findByUserName("userName").orElse(null);
		assertThat(foundUser).isNotNull();
		assertThat(foundUser.getUserName()).isEqualTo("userName");
	}

	@Test
	@DisplayName("사용자 수정 API")
	void updateUser_success() throws Exception {
		// given
		User savedUser = userRepository.save(new User("userName", "passWord"));
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}", savedUser.getId())
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.userName").value("userName"))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()));
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화")
	void updateUserStatus_success() throws Exception {
		// given
		User savedUser = new User("userName", "passWord");
		savedUser.dormantUser();
		userRepository.save(savedUser);
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("passWord", UserStatus.ACTIVE);

		// when & then
		mockMvc.perform(patch("/api/v1/users/{userId}/status", savedUser.getId())
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인")
	void loginUser_success() throws Exception {
		// given
		User savedUser = new User("userName", "passWord");
		userRepository.save(savedUser);
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");

		// when & then
		mockMvc.perform(post("/api/v1/users/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedUser.getId()))
				.andExpect(jsonPath("$.userName").value("userName"))
				.andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
				.andDo(print());
	}

}
