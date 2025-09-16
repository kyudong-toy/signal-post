package dev.kyudong.back.user.service;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 타 도메인에서 사용자 정보 요청시 사용됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserReaderService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public User getUserReference(Long userId) {
		return userRepository.getReferenceById(userId);
	}

	@Transactional(readOnly = true)
	public User getUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> {
					log.warn("요청 실패 - 존재하지 않는 사용자: username={}", username);
					return new UserNotFoundException(username);
				});
	}

	@Transactional(readOnly = true)
	public List<User> getUsersByIds(Set<Long> userIds) {
		return userRepository.findByIdIn(userIds);
	}

}
