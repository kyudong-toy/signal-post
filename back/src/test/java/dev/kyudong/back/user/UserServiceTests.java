package dev.kyudong.back.user;

import dev.kyudong.back.common.exception.InvalidInputException;
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

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("사용자 생성 - 성공")
	void createUser_success() {
		// given
		UserCreateReqDto request = new UserCreateReqDto("userName", "passWord");
		when(userRepository.existsByUserName("userName")).thenReturn(false);

		User mockUser = new User("userName", "passWord");
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// when
		UserCreateResDto userCreateResDto = userService.createUser(request);

		// then
		assertThat(userCreateResDto).isNotNull();
		assertThat(userCreateResDto.userName()).isEqualTo("userName");
		verify(userRepository, times(1)).existsByUserName(request.userName());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 생성 - 실패 : 이미 존재하는 userName")
	void createUser_fail() {
		// given
		UserCreateReqDto request = new UserCreateReqDto("userName", "passWord");
		when(userRepository.existsByUserName(request.userName())).thenReturn(true);

		// when
		assertThatThrownBy(() -> userService.createUser(request))
				.isInstanceOf(UserAlreadyExistsException.class)
				.hasMessage(request.userName() + " Already Exists");

		// then
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 수정 - 성공")
	void updateUser_success() {
		// given
		long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto("newPassWord");
		User mockUser = User.builder()
				.userName("userName")
				.passWord("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// when
		UserUpdateResDto userUpdateResDto = userService.updateUser(userId, request);

		// then
		assertThat(mockUser.getId()).isEqualTo(userUpdateResDto.id());
		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("사용자 수정 - 실패 : 존재하지 않는 사용자")
	void updateUser_fail_userNotFound() {
		// given
		long userId = 99L;
		UserUpdateReqDto request = new UserUpdateReqDto("newPassword");
		// 사용자가 없음을 시뮬레이션
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessage("User: {"+ userId + "} Not Found");
		verify(userRepository, times(1)).findById(userId);
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
		long userId = 1L;
		UserUpdateReqDto request = new UserUpdateReqDto(invalidPassword);

		// 우선 사용자는 찾아야 하므로, findById는 정상적으로 동작하도록 설정
		User mockUser = new User("userName", "passWord");
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// when & then
		assertThatThrownBy(() -> userService.updateUser(userId, request))
				.isInstanceOf(InvalidInputException.class);
	}

	@Test
	@DisplayName("사용자 상태 비활성화 => 활성화 - 성공")
	void updateUserStatus_success() {
		// given
		long userId = 1L; // url로 제공
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("passWord", UserStatus.ACTIVE);

		// 비활성화된 가짜 유저
		User mockUser = User.builder()
				.userName("userName")
				.passWord("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		mockUser.dormantUser();
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

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
		UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("passWord", UserStatus.ACTIVE);

		// 비활성화된 가짜 유저
		User mockUser = User.builder()
				.userName("userName")
				.passWord("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 99L);
		mockUser.dormantUser();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.updateUserStatus(userId, request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessage("User: {"+ userId + "} Not Found");
		verify(userRepository, times(1)).findById(userId);
	}

	@Test
	@DisplayName("사용자 로그인 - 성공")
	void loginUser_success() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");
		User mockUser = User.builder()
				.userName("userName")
				.passWord("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.findByUserName(request.userName())).thenReturn(Optional.of(mockUser));

		// when
		UserLoginResDto userLoginResDto = userService.loginUser(request);

		// then
		assertThat(userLoginResDto).isNotNull();
		assertThat(userLoginResDto.userName()).isEqualTo("userName");
		verify(userRepository, times(1)).findByUserName(request.userName());
	}

	@Test
	@DisplayName("사용자 로그인 - 실패 : 존재하지 않는 사용자")
	void loginUser_fail_userNotFound() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");
		when(userRepository.findByUserName(request.userName())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.loginUser(request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessage("User: {userName} Not Found");
	}

	@Test
	@DisplayName("사용자 로그인 - 실패 : 비밀번호 불일치")
	void loginUser_fail_invalid_password() {
		// given
		UserLoginReqDto request = new UserLoginReqDto("userName", "passWord");
		User mockUser = User.builder()
				.userName("userName")
				.passWord("diffPassWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		when(userRepository.findByUserName(request.userName())).thenReturn(Optional.of(mockUser));

		// when & then
		assertThatThrownBy(() -> userService.loginUser(request))
				.isInstanceOf(InvalidInputException.class)
				.hasMessage("Password not Equals");
	}

}
