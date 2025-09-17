package dev.kyudong.back.user.api;

import com.google.common.net.HttpHeaders;
import dev.kyudong.back.user.api.dto.UserLoginDto;
import dev.kyudong.back.user.api.dto.UserReissueDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.res.UserValidateResDto;
import dev.kyudong.back.user.properties.UserTokenProperties;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import dev.kyudong.back.user.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController implements UserAuthApi {

	private final UserAuthService userAuthService;
	private final UserTokenProperties userTokenProperties;

	@Override
	@PostMapping("/login")
	public ResponseEntity<UserValidateResDto> loginUser(@RequestBody @Valid UserLoginReqDto request) {
		UserLoginDto loginDto = userAuthService.login(request);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createRefreshCookie(loginDto.refreshToken()))
				.body(loginDto.response());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logoustUser(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal
	) {
		userAuthService.logout(userPrincipal);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/reissue")
	public ResponseEntity<UserValidateResDto> reissue(
			@RequestHeader("Authorization") String accessToken,
			@CookieValue("refresh") String refreshToken
	) {
		UserReissueDto reissueDto = userAuthService.reissue(accessToken, refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, createRefreshCookie(reissueDto.refreshToken()))
				.body(reissueDto.response());
	}

	private String createRefreshCookie(String refreshToken) {
		return ResponseCookie.from("refresh", refreshToken)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(userTokenProperties.getRefreshExpirationTime() / 1000)
				.sameSite("None")
				.build()
				.toString();
	}

}
