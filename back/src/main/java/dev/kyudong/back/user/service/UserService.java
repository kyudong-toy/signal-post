package dev.kyudong.back.user.service;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.user.api.dto.event.UserImageDeleteEvent;
import dev.kyudong.back.user.api.dto.event.UserImagePublishEvent;
import dev.kyudong.back.user.api.dto.req.*;
import dev.kyudong.back.user.api.dto.res.*;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public UserDetailResDto findUser(String username, Long requestUserId) {
		User user = findUserByUsername(username);

		log.debug("사용자 조회 요청: username={}, requestUserId={}", username, requestUserId);
		if (requestUserId == null) {
			return UserDetailResDto.of(user, false);
		}

		boolean isOwner = user.getId().equals(requestUserId);
		return UserDetailResDto.of(user, isOwner);
	}

	@Cacheable(value = "user", key = "#username", unless = "#result == null")
	public User findUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> {
					log.warn("사용자 요청 실패 - 존재하지 않는 사용자: username={}", username);
					return new UserNotFoundException(username);
				});
	}

	@Transactional
	public UserCreateResDto createUser(UserCreateReqDto request) {
		log.debug("사용자 생성 요청 시작, username: {}", request.username());

		if (userRepository.existsByUsername(request.username())) {
			log.warn("사용자 생성 실패 - 이름 중복 : username: {}", request.username());
			throw new UserAlreadyExistsException(request.username());
		}

		String encodePassword = passwordEncoder.encode(request.password());
		User newUser = User.create(request.username(), request.password(), encodePassword, request.displayName());

		User savedUser = userRepository.save(newUser);
		log.debug("사용자 생성 성공, id: {}, username: {}", savedUser.getId(), savedUser.getUsername());
		return UserCreateResDto.from(savedUser);
	}

	@Transactional
	@CacheEvict(value = "user", key = "#username")
	public UserProfileUpdateResDto updateProfile(final String username, UserProfileUpdateReqDto request) {
		log.debug("사용자 수정 요청 시작: username={}", username);

		User user = findUserByUsername(username);

		user.updateDisplayName(request.displayName());
		user.updateBio(request.bio());

		user.updateBackGroundImage(request.backGroundImageUrl());
		if (request.backGroundImageId() != null
				&& !(request.backGroundImageId().equals(request.prevBackGroundImageId())))
		{
			UserImagePublishEvent publishEvent = UserImagePublishEvent.of(user.getId(), request.backGroundImageId());
			eventPublisher.publishEvent(publishEvent);

			UserImageDeleteEvent deleteEvent = UserImageDeleteEvent.of(user.getId(), request.prevBackGroundImageId());
			eventPublisher.publishEvent(deleteEvent);
		}

		user.updateProfileImage(request.profileImageUrl());
		if (request.profileImageId() != null
				&& !(request.profileImageId().equals(request.prevProfileImageId())))
		{
			UserImagePublishEvent publishEvent = UserImagePublishEvent.of(user.getId(), request.profileImageId());
			eventPublisher.publishEvent(publishEvent);

			UserImageDeleteEvent deleteEvent = UserImageDeleteEvent.of(user.getId(), request.prevProfileImageId());
			eventPublisher.publishEvent(deleteEvent);
		}

		log.debug("사용자 수정 성공, id: {}", user.getId());
		return UserProfileUpdateResDto.from(user);
	}

	@Transactional
	@CacheEvict(value = "user", key = "#username")
	public UserPasswordUpdateResDto updatePassword(final String username, UserPasswordUpdateReqDto request) {
		log.debug("사용자 비밀번호 수정 요청 시작: username={}", username);

		User user = findUserByUsername(username);

		String encodedPassword = passwordEncoder.encode(request.password());
		user.updatepassWord(request.password(), encodedPassword);

		log.debug("사용자 수정 성공, id: {}", user.getId());
		return UserPasswordUpdateResDto.from(user);
	}

	@Transactional
	@CacheEvict(value = "user", key = "#username")
	public UserStatusUpdateResDto updateUserStatus(final String username, UserStatusUpdateReqDto request) {
		log.debug("사용자 상태 수정 요청 시작: username={}, status={}", username, request.userStatus());

		User user = findUserByUsername(username);

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

		log.debug("사용자 상태 수정 요청 성공, id: {}, status: {}", user.getId(), user.getStatus());
		return UserStatusUpdateResDto.from(user);
	}

}
