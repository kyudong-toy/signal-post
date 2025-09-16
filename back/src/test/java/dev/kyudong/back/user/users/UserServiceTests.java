package dev.kyudong.back.user.users;

import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.user.api.dto.req.*;
import dev.kyudong.back.user.api.dto.res.*;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import dev.kyudong.back.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

public class UserServiceTests extends UnitTestBase {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Nested
	@DisplayName("사용자 조회")
	class FindUser {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			final String username = "mockUser";
			User mockUser = createMockUser();
			given(userRepository.findByUsername(anyString())).willReturn(Optional.of(mockUser));

			// when
			UserDetailResDto response = userService.findUser(username, 1L);

			// then
			assertThat(response).isNotNull();
			assertThat(response.username()).isEqualTo(username);
			then(userRepository).should().findByUsername(username);
		}

		@Test
		@DisplayName("실패 : 사용자를 찾을 수 없음")
		void fail_userNotFound() {
			// given
			User mockUser = createMockUser();
			given(userRepository.findByUsername(anyString())).willThrow(new UserNotFoundException(mockUser.getUsername()));

			// when && then
			assertThatThrownBy(() -> userService.findUser("nonexsistsUser", 1L))
					.isInstanceOf(UserNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("사용자 생성")
	class CreateUser {

		@Test
		@DisplayName("사용자 생성 - 성공")
		void success() {
			// given
			UserCreateReqDto request = new UserCreateReqDto("username", "password");
			given(userRepository.existsByUsername(request.username())).willReturn(false);

			String encodedPassword = "encodedPassword";
			given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
			given(userRepository.save(any(User.class))).will(invocation -> {
				User savedUser = invocation.getArgument(0);
				return defaultUserSetting(savedUser);
			});

			// when
			UserCreateResDto userCreateResDto = userService.createUser(request);

			// then
			assertThat(userCreateResDto).isNotNull();
			then(userRepository).should().existsByUsername(request.username());
			then(userRepository).should().save(argThat(user -> (
					user.getUsername().equals(request.username()) &&
					user.getPassword().equals(encodedPassword) &&
					user.getStatus() == UserStatus.ACTIVE
			)));
		}

		@Test
		@DisplayName("실패 : 이미 사용중인 username")
		void fail_alreadyExistsUsername() {
			// given
			UserCreateReqDto request = new UserCreateReqDto("username", "password");
			given(userRepository.existsByUsername(request.username())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> userService.createUser(request))
					.isInstanceOf(UserAlreadyExistsException.class)
					.hasMessageContaining(request.username());
			then(userRepository).should(never()).save(any(User.class));
		}

		@ParameterizedTest
		@DisplayName("실패 : 유효하지 않은 사용자 이름")
		@ValueSource(strings = {
				"",
				"   ",
				"abc",
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
		})
		void fail_invalidUsername(String invalidUsername) {
			// given
			UserCreateReqDto request = new UserCreateReqDto(invalidUsername, "password");
			when(userRepository.existsByUsername(request.username())).thenReturn(true);

			// when
			assertThatThrownBy(() -> userService.createUser(request))
					.isInstanceOf(UserAlreadyExistsException.class)
					.hasMessageContaining(request.username());

			// then
			then(userRepository).should(never()).save(any(User.class));
		}

	}

	@Nested
	@DisplayName("사용자 프로필")
	class UpdateProfile {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));

			UserProfileUpdateReqDto request = new UserProfileUpdateReqDto(
					"테스트",
					"자기소개",
					null,
					null,
					null,
					null,
					null,
					null
			);

			// when
			UserProfileUpdateResDto userProfileUpdateResDto = userService.updateProfile(mockUser.getUsername(), request);

			// then
			assertThat(mockUser.getId()).isEqualTo(userProfileUpdateResDto.id());
			then(userRepository).should().findByUsername(mockUser.getUsername());
		}

		@Test
		@DisplayName("실패 : 사용자를 찾을 수 없음")
		void fail_userNotFound() {
			// given
			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.empty());

			UserProfileUpdateReqDto request = new UserProfileUpdateReqDto(
					"테스트",
					"자기소개",
					null,
					null,
					null,
					null,
					null,
					null
			);

			// when & then
			assertThatThrownBy(() -> userService.updateProfile(mockUser.getUsername(), request))
					.isInstanceOf(UserNotFoundException.class);
		}

	}

	@Nested
	@DisplayName("사용자 비밀번호 수정")
	class UpdatePasword {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newPassword");

			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.encode(request.password())).willReturn("encodedNewPassword");

			// when
			UserPasswordUpdateResDto UserPasswordUpdateResDto = userService.updatePassword(mockUser.getUsername(), request);

			// then
			assertThat(mockUser.getId()).isEqualTo(UserPasswordUpdateResDto.id());
			assertThat(mockUser.getPassword()).isEqualTo("encodedNewPassword");
			then(userRepository).should(times(1)).findByUsername(anyString());
			then(passwordEncoder).should(times(1)).encode(anyString());
		}

		@Test
		@DisplayName("실패 : 존재하지 않는 사용자")
		void fail_userNotFound() {
			// given
			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.empty());
			UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto("newPassword");

			// when & then
			assertThatThrownBy(() -> userService.updatePassword(mockUser.getUsername(), request))
					.isInstanceOf(UserNotFoundException.class);
		}

		@ParameterizedTest
		@DisplayName("실패 : 유효하지 않은 패스워드")
		@ValueSource(strings = {
				"",
				"   ",
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
		})
		void fail_invalidPassword(String invalidPassword) {
			// given
			UserPasswordUpdateReqDto request = new UserPasswordUpdateReqDto(invalidPassword);

			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.encode(request.password())).willReturn("encodedNewPassword");

			// when & then
			assertThatThrownBy(() -> userService.updatePassword(mockUser.getUsername(), request))
					.isInstanceOf(InvalidInputException.class);
		}

	}

	@Nested
	@DisplayName("사용자 상태 제어")
	class UpdateUserStatus {

		@Test
		@DisplayName("사용자 상태 비활성화 => 활성화 - 성공")
		void updateUserStatus_success() {
			// given
			UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);

			User mockUser = createMockUser();
			mockUser.dormantUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.matches(request.password(), mockUser.getPassword())).willReturn(true);

			// when
			UserStatusUpdateResDto userStatusUpdateResDto = userService.updateUserStatus(mockUser.getUsername(), request);

			// then
			assertThat(userStatusUpdateResDto).isNotNull();
			assertThat(userStatusUpdateResDto.status()).isEqualTo(UserStatus.ACTIVE);
		}

		@Test
		@DisplayName("사용자 상태 비활성화 => 활성화 - 실패 : 존재하지 않는 사용자")
		void updateUserStatus_ACTIVE_fail_userNotFound() {
			// given
			final String nonUser = "nonUser";
			UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);
			given(userRepository.findByUsername(nonUser)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.updateUserStatus(nonUser, request))
					.isInstanceOf(UserNotFoundException.class);
			then(userRepository).should().findByUsername(nonUser);
		}

		@Test
		@DisplayName("사용자 상태 비활성화 => 활성화 - 실패 : 비밀번호가 일치하지 않음")
		void updateUserStatus_ACTIVE_fail_passwordIncollect() {
			// given
			UserStatusUpdateReqDto request = new UserStatusUpdateReqDto("password", UserStatus.ACTIVE);

			User mockUser = createMockUser();
			given(userRepository.findByUsername(mockUser.getUsername())).willReturn(Optional.of(mockUser));
			given(passwordEncoder.matches(request.password(), mockUser.getPassword())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> userService.updateUserStatus(mockUser.getUsername(), request))
					.isInstanceOf(InvalidInputException.class);
		}

	}

}
