package dev.kyudong.back.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.UserAuthController;
import dev.kyudong.back.user.api.dto.UserLoginDto;
import dev.kyudong.back.user.api.dto.UserReissueDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.res.UserValidateResDto;
import dev.kyudong.back.user.exception.InvalidTokenException;
import dev.kyudong.back.user.properties.UserTokenProperties;
import dev.kyudong.back.user.service.UserAuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.*;

@WebMvcTest(UserAuthController.class)
@Import(SecurityConfig.class)
public class UserAuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserAuthService userAuthService;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserTokenProperties userTokenProperties;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("사용자 로그인 - 성공")
	void loginApi_success() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		UserLoginDto response = new UserLoginDto(UserValidateResDto.from("access"), "refresh");
		given(userAuthService.login(any(UserLoginReqDto.class))).willReturn(response);

		// when & given
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("사용자 로그인 - 실패")
	void loginApi_fail() throws Exception {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		when(userAuthService.login(any(UserLoginReqDto.class)))
				.thenThrow(new InvalidInputException("Password not Equals"));

		// when & given
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}

	@Test
	@DisplayName("토큰 재발급 - 성공")
	void reissueApi_success() throws Exception {
		// given
		String accessToken = "access";
		String refreshValue = "refresh";

		UserReissueDto reissueDto = new UserReissueDto(UserValidateResDto.from("new-access"), "new-refresh");
		given(userAuthService.reissue(anyString(), anyString())).willReturn(reissueDto);

		// when & given
		mockMvc.perform(post("/api/v1/auth/reissue")
						.header(HttpHeaders.AUTHORIZATION, accessToken)
						.cookie(new Cookie("refresh", refreshValue)))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@DisplayName("토큰 재발급 - 실패")
	void reissueApi_fail() throws Exception {
		// given
		String accessToken = "access";
		String refreshValue = "refresh";

		given(userAuthService.reissue(anyString(), anyString()))
				.willThrow(new InvalidTokenException());

		// when & given
		mockMvc.perform(post("/api/v1/auth/reissue")
						.header(HttpHeaders.AUTHORIZATION, accessToken)
						.cookie(new Cookie("refresh", refreshValue)))
				.andExpect(status().isUnauthorized())
				.andDo(print());
	}

}
