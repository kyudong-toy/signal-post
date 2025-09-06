package dev.kyudong.back.user.service;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.UserCreateResDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserStatusUpdateResDto;
import dev.kyudong.back.user.api.dto.res.UserUpdateResDto;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Transactional
	public UserCreateResDto createUser(UserCreateReqDto request) {
		log.debug("사용자 생성 요청 시작, username: {}", request.username());

		if (userRepository.existsByUsername(request.username())) {
			log.warn("사용자 생성 실패 - 이름 중복 : username: {}", request.username());
			throw new UserAlreadyExistsException(request.username());
		}

		User newUser = User.builder()
				.username(request.username())
				.rawPassword(request.password())
				.encodedPassword(passwordEncoder.encode(request.password()))
				.build();

		User savedUser = userRepository.save(newUser);
		log.info("사용자 생성 성공, id: {}, username: {}", savedUser.getId(), savedUser.getUsername());
		return UserCreateResDto.from(savedUser);
	}

	@Transactional(readOnly = true)
	public UserLoginResDto loginUser(UserLoginReqDto request) {
		log.debug("사용자 로그인 요청 시작, username: {}", request.username());

		User user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> {
					log.warn("사용자 로그인 요청 실패 - 존재하지 않는 사용자 : username: {}", request.username());
					return new UserNotFoundException(request.username());
				});

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			log.warn("사용자 로그인 요청 실패 - 비밀번호가 일치하지 않습니다 : username: {}", request.username());
			throw new InvalidInputException("Password not Equals");
		}

		log.info("사용자 로그인 요청 성공, id: {}, status: {}", user.getId(), user.getStatus());
		return UserLoginResDto.from(user, jwtUtil.generateToken(user));
	}

	@Transactional
	public UserUpdateResDto updateUser(final Long userId, UserUpdateReqDto reqeust) {
		log.debug("사용자 수정 요청 시작, id: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("사용자 수정 실패 - 존재하지 않는 사용자 : id: {}", userId);
					return new UserNotFoundException(userId);
				});

		// 1. 비밀번호 수정
		String encodedPassword = passwordEncoder.encode(reqeust.password());
		user.updatepassWord(reqeust.password(), encodedPassword);

		log.info("사용자 수정 성공, id: {}", user.getId());
		return UserUpdateResDto.from(user);
	}

	@Transactional
	public UserStatusUpdateResDto updateUserStatus(final Long userId, UserStatusUpdateReqDto request) {
		log.debug("사용자 상태 수정 요청 시작, id: {}, status: {}", userId, request.userStatus());

		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("사용자 수정 실패 - 존재하지 않는 사용자 : id: {}", userId);
					return new UserNotFoundException(userId);
				});

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			log.warn("사용자 수정 실패 - 비밀번호가 일치하지 않습니다 : id: {}", user.getId());
			throw new InvalidInputException("Password not Equals");
		}

		switch (request.userStatus()) {
			case ACTIVE -> user.activeUser();
			case DORMANT -> user.dormantUser();
			case DELETED -> user.deleteUser();
			default -> {
				log.warn("응답할 수 없는 사용자 상태 요청 : id: {}, status: {}", user.getId(), request.userStatus());
				throw new InvalidInputException("UserStatus Cant not be update");
			}
		}

		log.info("사용자 상태 수정 요청 성공, id: {}, status: {}", user.getId(), user.getStatus());
		return UserStatusUpdateResDto.from(user);
	}

	@Transactional(readOnly = true)
	public User getUserProxy(Long userId) {
		return userRepository.getReferenceById(userId);
	}

}
