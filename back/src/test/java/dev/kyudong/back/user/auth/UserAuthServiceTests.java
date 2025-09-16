package dev.kyudong.back.user.auth;

import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.UserLoginDto;
import dev.kyudong.back.user.api.dto.UserReissueDto;
import dev.kyudong.back.user.api.dto.req.*;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.exception.InvalidTokenException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.repository.UserTokenRepository;
import dev.kyudong.back.user.service.UserAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

public class UserAuthServiceTests extends UnitTestBase {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private UserAuthService userAuthService;

	@Mock
	private UserTokenRepository userTokenRepository;

	@Nested
	@DisplayName("사용자 로그인")
	class Login {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			final String username = "mockUser";
			UserLoginReqDto request = new UserLoginReqDto(username, "password");
			User mockUser = createMockUser();

			given(userRepository.findByUsername(request.username())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.matches(request.password(), mockUser.getPassword())).willReturn(true);

			String accessToken = "access";
			given(jwtUtil.createAccessToken(mockUser)).willReturn(accessToken);

			String refreshToken = "refresh";
			given(jwtUtil.createRefreshToken(mockUser)).willReturn(refreshToken);

			// when
			UserLoginDto loginDto = userAuthService.login(request);

			// then
			assertThat(loginDto).isNotNull();
			assertThat(loginDto.response().username()).isEqualTo(username);
			assertThat(loginDto.response().token()).isNotNull();
			assertThat(loginDto.refreshToken()).isNotNull();

			then(userRepository).should().findByUsername(request.username());
			then(userTokenRepository).should().saveToken(request.username(), refreshToken);

			then(jwtUtil).should().createAccessToken(any(User.class));
			then(jwtUtil).should().createRefreshToken(any(User.class));
		}

		@Test
		@DisplayName("실패 : 존재하지 않는 사용자")
		void fail_userNotFound() {
			// given
			UserLoginReqDto request = new UserLoginReqDto("username", "password");
			when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userAuthService.login(request))
					.isInstanceOf(UserNotFoundException.class);
		}

		@Test
		@DisplayName("실패 : 비밀번호 불일치")
		void fail_invalid_password() {
			// given
			UserLoginReqDto request = new UserLoginReqDto("mockUser", "password");
			User mockUser = createMockUser();
			given(userRepository.findByUsername(request.username())).willReturn(Optional.of(mockUser));

			// when & then
			assertThatThrownBy(() -> userAuthService.login(request))
					.isInstanceOf(InvalidInputException.class);
		}

	}

	@Nested
	@DisplayName("토큰 재발급")
	class Reissue {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			final String accessToken = "access";
			final String refreshToken = "refresh";

			given(jwtUtil.validateRefreshToken(anyString())).willReturn(true);

			String username = "mockUser";
			Claims claims = Jwts.claims().setSubject(username);
			claims.put("id", 1L);
			claims.put("role", UserRole.USER);
			claims.put("status", UserStatus.ACTIVE);
			given(jwtUtil.getClaimsFromAccessToken(anyString())).willReturn(claims);

			String storedRefreshToken = "refresh";
			given(userTokenRepository.findTokenByUsername(username)).willReturn(storedRefreshToken);

			User mockUser = createMockUser();
			given(userRepository.findByUsername(username)).willReturn(Optional.of(mockUser));

			String newAccess = "newAccess";
			given(jwtUtil.createAccessToken(any(User.class))).willReturn(newAccess);

			String newRefresh = "newRefresh";
			given(jwtUtil.createRefreshToken(any(User.class))).willReturn(newRefresh);

			// when
			UserReissueDto reissueDto = userAuthService.reissue(accessToken, refreshToken);

			// then
			assertThat(reissueDto).isNotNull();
			assertThat(reissueDto.response().token()).isEqualTo(newAccess);
			assertThat(reissueDto.refreshToken()).isEqualTo(newRefresh);

			then(userTokenRepository).should().findTokenByUsername(anyString());
			then(userRepository).should().findByUsername(anyString());
			then(jwtUtil).should().createAccessToken(any(User.class));
			then(jwtUtil).should().createRefreshToken(any(User.class));
			then(userTokenRepository).should().saveToken(anyString(), anyString());
		}

		@Test
		@DisplayName("실패")
		void fail() {
			// given
			final String accessToken = "access";
			final String refreshToken = "refresh?";

			given(jwtUtil.validateRefreshToken(anyString())).willReturn(true);

			String username = "mockUser";
			Claims claims = Jwts.claims().setSubject(username);
			claims.put("id", 1L);
			claims.put("role", UserRole.USER);
			claims.put("status", UserStatus.ACTIVE);
			given(jwtUtil.getClaimsFromAccessToken(anyString())).willReturn(claims);

			String storedRefreshToken = "refresh";
			given(userTokenRepository.findTokenByUsername(username)).willReturn(storedRefreshToken);

			// when
			assertThatThrownBy(() -> userAuthService.reissue(accessToken, refreshToken))
					.isInstanceOf(InvalidTokenException.class);

			// then
			then(userTokenRepository).should().findTokenByUsername(anyString());
			then(userRepository).should(never()).findByUsername(anyString());
			then(jwtUtil).should(never()).createAccessToken(any(User.class));
			then(jwtUtil).should(never()).createRefreshToken(any(User.class));
			then(userTokenRepository).should(never()).saveToken(anyString(), anyString());
		}

	}

}
