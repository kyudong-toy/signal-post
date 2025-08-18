package dev.kyudong.back.user;

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
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("사용자 생성 - 성공")
	void createUser_success() {
		// given
		UserCreateReqDto request = new UserCreateReqDto("username", "password");
		when(userRepository.existsByUsername("username")).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

		User mockUser = User.builder()
				.username(request.username())
				.rawPassword(request.password())
				.encodedPassword(passwordEncoder.encode(request.password()))
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.save(any(User.class))).thenReturn(mockUser);


		// when
		UserCreateResDto userCreateResDto = userService.createUser(request);

		// then
		assertThat(userCreateResDto).isNotNull();
		assertThat(userCreateResDto.username()).isEqualTo("username");
		verify(userRepository, times(1)).existsByUsername(request.username());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 생성 - 실패 : 이미 사용중인 username")
	void createUser_fail_alreadyExistsUsername() {
		// given
		UserCreateReqDto request = new UserCreateReqDto("username", "password");
		when(userRepository.existsByUsername(request.username())).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(UserAlreadyExistsException.class)
				.hasMessageContaining(request.username());
		verify(userRepository, never()).save(any(User.class));
	}

	// 사용자 패스워드 요청에 사용
	private static Stream<Arguments> provideInvalidUsernames() {
		return Stream.of(
				Arguments.of(""),          // 1. 빈 문자열 ""
				Arguments.of(" "),         // 2. 공백 문자 " "
				Arguments.of("a".repeat(31))    // 3. 31자 문자열
		);
	}

	@ParameterizedTest
	@DisplayName("사용자 생성 - 실패 : 유효하지 않은 사용자 이름")
	@MethodSource("provideInvalidUsernames")
	void createUser_fail_invalidUsername(String invalidUsername) {
		// given
		UserCreateReqDto request = new UserCreateReqDto(invalidUsername, "password");
		when(userRepository.existsByUsername(request.username())).thenReturn(true);

		// when
		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(UserAlreadyExistsException.class)
				.hasMessageContaining(request.username());

		// then
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 수정 - 성공")
	void updateUser_success() {
		// given
		final Long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");
		when(passwordEncoder.encode(request.password())).thenReturn("encodedNewPassword");
		User mockUser = User.builder()
				.username("username")
				.rawPassword(request.password())
				.encodedPassword(request.password())
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// when
		UserUpdateResDto userUpdateResDto = userService.updateUser(userId, request);

		// then
		assertThat(mockUser.getId()).isEqualTo(userUpdateResDto.id());
		assertThat(mockUser.getPassword()).isEqualTo("encodedNewPassword");
		verify(userRepository, times(1)).findById(userId);
		verify(passwordEncoder, times(1)).encode(request.password());
	}

	@Test
	@DisplayName("사용자 수정 - 실패 : 존재하지 않는 사용자")
	void updateUser_fail_userNotFound() {
		// given
		final Long userId = 999L;
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(String.valueOf(userId));
		verify(userRepository, times(1)).findById(userId);
		verify(passwordEncoder, never()).encode(request.password());
	}

	// 사용자 패스워드 요청에 사용
	private static Stream<Arguments> provideInvalidPasswords() {
		return Stream.of(
				Arguments.of((String) null),       		// 1. null
				Arguments.of(""),          // 2. 빈 문자열 ""
				Arguments.of(" "),         // 3. 공백 문자 " "
				Arguments.of("a".repeat(151))    // 4. 151자 문자열 (동적 생성)
		);
	}

	@ParameterizedTest
	@DisplayName("사용자 수정 - 실패 : 유효하지 않은 패스워드")
	@MethodSource("provideInvalidPasswords")
	void updateUser_fail_invalidPassword(String invalidPassword) {
		// given
		final Long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto(invalidPassword);

		User mockUser = User.builder()
				.username("username")
				.rawPassword("password")
				.encodedPassword("password")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(passwordEncoder.encode(request.password())).thenReturn("encodedNewPassword");

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request))
				.isInstanceOf(InvalidInputException.class);
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화 - 성공")
	void updateUserStatus_success() {
		// given
		final Long userId = 1L;
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);

		User mockUser = User.builder()
				.username("username")
				.rawPassword(request.password())
				.encodedPassword(request.password())
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		mockUser.dormantUser();
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);

		// when
		UserStatusUpdateResDto userStatusUpdateResDto = userService.updateUserStatus(userId, request);

		// then
		assertThat(userStatusUpdateResDto).isNotNull();
		assertThat(userStatusUpdateResDto.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화 - 실패 : 존재하지 않는 사용자")
	void updateUserStatus_ACTIVE_fail_userNotFound() {
		// given
		long userId = 1L; // url로 제공
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUserStatus(userId, request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(String.valueOf(userId));
		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화 - 실패 : 비밀번호가 일치하지 않음")
	void updateUserStatus_ACTIVE_fail_passwordIncollect() {
		// given
		long userId = 1L; // url로 제공
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);

		User mockUser = User.builder()
				.username("username")
				.rawPassword(request.password())
				.encodedPassword(request.password())
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> userService.updateUserStatus(userId, request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessage("Password not Equals");
	}

	@Test
	@DisplayName("사용자 로그인 - 성공")
	void loginUser_success() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
		User mockUser = User.builder()
				.username("username")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode(request.password()))
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));
		when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);
		when(jwtUtil.generateToken(mockUser)).thenReturn("Example Token");

		// when
		UserLoginResDto userLoginResDto = userService.loginUser(request);

		// then
		assertThat(userLoginResDto).isNotNull();
		assertThat(userLoginResDto.username()).isEqualTo("username");
		assertThat(userLoginResDto.token()).isNotNull();
		verify(userRepository, times(1)).findByUsername(request.username());
		verify(jwtUtil, times(1)).generateToken(mockUser);
	}

	@Test
	@DisplayName("사용자 로그인 - 실패 : 존재하지 않는 사용자")
	void loginUser_fail_userNotFound() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.loginUser(request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(request.username());
	}

	@Test
	@DisplayName("사용자 로그인 - 실패 : 비밀번호 불일치")
	void loginUser_fail_invalid_password() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("username", "password");
		User mockUser = User.builder()
				.username("username")
				.rawPassword("diffPassWord")
				.encodedPassword("diffPassWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));

		// when & then
		assertThatThrownBy(() -> userService.loginUser(request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessage("Password not Equals");
	}

}
