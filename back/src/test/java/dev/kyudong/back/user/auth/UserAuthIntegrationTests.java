package dev.kyudong.back.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.res.UserValidateResDto;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.repository.UserTokenRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserAuthIntegrationTests extends IntegrationTestBase {

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

	@Autowired
	private UserTokenRepository userTokenRepository;

	@Test
	@DisplayName("사용자 로그인")
	void login() throws Exception {
		// given
		User newUser = User.create("mockUser", "password", passwordEncoder.encode("password"));
		User savedUser = userRepository.save(newUser);
		userRepository.save(savedUser);
		UserLoginReqDto request = new UserLoginReqDto(savedUser.getUsername(), "password");

		// when
		String cookieName = "refresh";
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(cookie().exists(cookieName))
				.andExpect(cookie().httpOnly(cookieName, true))
				.andExpect(cookie().secure(cookieName, true))
				.andExpect(cookie().path(cookieName, "/"))
				.andDo(print())
				.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		UserValidateResDto response = objectMapper.readValue(responseBody, UserValidateResDto.class);
		assertThat(response).isNotNull();
	}

	@Test
	@DisplayName("토큰 재발급")
	void reissue() throws Exception {
		// given
		User newUser = User.create("mockUser", "password", passwordEncoder.encode("password"));
		User savedUser = userRepository.save(newUser);
		userRepository.save(savedUser);
		String accessToken = jwtUtil.createAccessToken(savedUser);
		String refreshToken = jwtUtil.createRefreshToken(savedUser);

		userTokenRepository.saveToken(savedUser.getUsername(), refreshToken);

		// when & then
		String cookieName = "refresh";
		mockMvc.perform(post("/api/v1/auth/reissue")
						.header(HttpHeaders.AUTHORIZATION, accessToken)
						.cookie(new Cookie("refresh", refreshToken)))
				.andExpect(status().isOk())
				.andExpect(cookie().exists(cookieName))
				.andExpect(cookie().httpOnly(cookieName, true))
				.andExpect(cookie().secure(cookieName, true))
				.andExpect(cookie().path(cookieName, "/"))
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andDo(print())
				.andReturn();
	}

}
