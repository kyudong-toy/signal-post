package dev.kyudong.back.user.security;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> {
					log.warn("사용자 요청 실패 - 존재하지 않는 사용자 : username: {}", username);
					return new UserNotFoundException(username);
				});

		return CustomUserPrincipal.builder()
				.id(user.getId())
				.username(user.getUsername())
				.password(user.getPassword())
				.status(user.getStatus())
				.role(user.getRole())
				.build();
	}

}
