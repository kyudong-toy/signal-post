package dev.kyudong.back.user.service;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.UserLoginDto;
import dev.kyudong.back.user.api.dto.UserReissueDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserReissueResDto;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.InvalidTokenException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.repository.UserTokenRepository;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserTokenRepository userTokenRepository;

	@Transactional(readOnly = true)
	public UserLoginDto login(UserLoginReqDto request) {
		log.debug("사용자 로그인 요청 시작: username={}", request.username());

		User user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> {
					log.warn("사용자 로그인 요청 실패 - 존재하지 않는 사용자: username={}", request.username());
					return new UserNotFoundException(request.username());
				});

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			log.warn("사용자 로그인 요청 실패 - 비밀번호가 일치하지 않습니다 : username: {}", request.username());
			throw new InvalidInputException("Password not Equals");
		}

		UserLoginResDto response = UserLoginResDto.from(user, jwtUtil.createAccessToken(user));
		String refreshToken = jwtUtil.createRefreshToken(user);
		userTokenRepository.saveToken(request.username(), refreshToken);

		log.debug("사용자 로그인 요청 성공: id={}, status={}", user.getId(), user.getStatus());
		return UserLoginDto.from(response, refreshToken);
	}

	@Transactional
	public void logout(CustomUserPrincipal principal) {
		log.debug("사용자 로그아웃 요청 시작: username={}", principal.getUsername());
		userTokenRepository.deleteToken(principal.getUsername());
	}

	@Transactional
	public UserReissueDto reissue(@NonNull final String accessToken, @NonNull final String refreshToken) {
		if (!jwtUtil.validateRefreshToken(refreshToken)) {
			log.warn("유효하지 않는 토큰입니다");
			throw new InvalidTokenException();
		}

		String username = jwtUtil.getClaimsFromAccessToken(accessToken).getSubject();
		String storedRefreshToken = userTokenRepository.findTokenByUsername(username);
		if (storedRefreshToken == null) {
			log.warn("서버에 존재하지 않는 토큰입니다: username={}", username);
			throw new InvalidTokenException();
		}

		if (!refreshToken.equals(storedRefreshToken)) {
			log.error("토큰이 일치하지 않아 보안 조치로 토큰을 삭제합니다: username={}", username);
			userTokenRepository.deleteToken(username);
			throw new InvalidTokenException();
		}

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> {
					log.warn("토큰 재발급 요청 실패 - 존재하지 않는 사용자: username={}", username);
					return new UserNotFoundException(username);
				});

		String newAccessToken = jwtUtil.createAccessToken(user);
		String newRefreshToken = jwtUtil.createRefreshToken(user);

		userTokenRepository.saveToken(username, newRefreshToken);
		UserReissueResDto reissueDto = UserReissueResDto.from(newAccessToken);

		log.debug("토큰이 다시 발급되었습니다: username={}", username);
		return UserReissueDto.from(reissueDto, newRefreshToken);
	}

}
