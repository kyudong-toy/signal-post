package dev.kyudong.back.follow;

import dev.kyudong.back.follow.api.res.FollowAcceptResDto;
import dev.kyudong.back.follow.api.res.FollowBlokcedResDto;
import dev.kyudong.back.follow.api.res.FollowCreateResDto;
import dev.kyudong.back.follow.api.res.FollowDeleteResDto;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.exception.FollowingException;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.follow.service.FollowService;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTests {

	@Mock
	private FollowRepository followRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FollowService followService;

	private User makeMockUser(Long id, String username) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	@Test
	@DisplayName("팔로잉 요청 - 성공")
	void createFollow_success() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));
		given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class)))
				.willReturn(false);

		Follow mockFollow = Follow.builder()
				.follower(mockFollower)
				.following(mockFollowing)
				.build();
		given(followRepository.save(any(Follow.class))).willReturn(mockFollow);

		// when
		FollowCreateResDto followCreateResDto = followService.createFollow(
				mockFollower.getId(), mockFollowing.getUsername());

		// then
		assertThat(followCreateResDto.followerId()).isEqualTo(mockFollower.getId());
		assertThat(followCreateResDto.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(followCreateResDto.status()).isEqualTo(FollowStatus.PENDING);
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
		then(followRepository).should(times(1)).existsByFollowerAndFollowing(any(User.class), any(User.class));
		then(followRepository).should(times(1)).save(any(Follow.class));
	}

	@Test
	@DisplayName("팔로잉 요청 - 실패 : 자기 자신을 팔로잉 시도함")
	void createFollow_fail_selfFollowing() {
		// given
		User mockFollower = makeMockUser(1L, "Hello");
		User mockFollowing = makeMockUser(1L, "Hello");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));

		// when & then
		assertThatThrownBy(() -> followService.createFollow(mockFollower.getId(), mockFollowing.getUsername()))
						.isInstanceOf(FollowingException.class)
						.hasMessage("자기 자신을 팔로잉 할 수 없습니다");
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
		then(followRepository).should(never()).existsByFollowerAndFollowing(any(User.class), any(User.class));
		then(followRepository).should(never()).save(any(Follow.class));
	}

	@Test
	@DisplayName("팔로잉 요청 - 실패 : 팔로잉 상대가 존재하지 않음")
	void createFollow_fail_followerNotFound() {
		// given
		User mockFollower = makeMockUser(999L, "follower");
		User mockFollowing = makeMockUser(1L, "ghost");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> followService.createFollow(mockFollower.getId(), mockFollowing.getUsername()))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(mockFollowing.getUsername());
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
		then(followRepository).should(never()).existsByFollowerAndFollowing(any(User.class), any(User.class));
		then(followRepository).should(never()).save(any(Follow.class));
	}

	@Test
	@DisplayName("팔로잉 요청 - 실패 : 이미 팔로잉 중임")
	void createFollow_fail_alreadyFollowing() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));
		given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class)))
				.willReturn(true);

		// when & then
		assertThatThrownBy(() -> followService.createFollow(mockFollower.getId(), mockFollowing.getUsername()))
				.isInstanceOf(AlreadyFollowException.class);
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
		then(followRepository).should(times(1)).existsByFollowerAndFollowing(any(User.class), any(User.class));
		then(followRepository).should(never()).save(any(Follow.class));
	}

	@Test
	@DisplayName("팔로잉 승낙 요청 - 성공")
	void acceptFollow_success() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));

		Follow mockFollow = Follow.builder()
				.follower(mockFollower)
				.following(mockFollowing)
				.build();
		given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
				.willReturn(Optional.ofNullable(mockFollow));

		// when
		FollowAcceptResDto response = followService.acceptFollow(mockFollower.getId(), mockFollowing.getUsername());

		// then
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.FOLLOWING);
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
	}

	@Test
	@DisplayName("팔로잉 차단 요청 - 성공")
	void blockedFollow_success() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));

		Follow mockFollow = Follow.builder()
				.follower(mockFollower)
				.following(mockFollowing)
				.build();
		given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
				.willReturn(Optional.ofNullable(mockFollow));

		// when
		FollowBlokcedResDto response = followService.blokcFollow(mockFollower.getId(), mockFollowing.getUsername());

		// then
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.BLOCKED);
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
	}

	@Test
	@DisplayName("팔로잉 취소 요청 - 성공")
	void deleteFollow_success() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));

		Follow mockFollow = Follow.builder()
				.follower(mockFollower)
				.following(mockFollowing)
				.build();
		given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
				.willReturn(Optional.ofNullable(mockFollow));

		// when
		FollowDeleteResDto response = followService.deleteFollow(mockFollower.getId(), mockFollowing.getUsername());

		// then
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.UNFOLLOWED);
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
	}

	@Test
	@DisplayName("팔로잉 취소 요청 - 실패 : 팔로잉이 서로 안되있음")
	void deleteFollow_fail_notFollowing() {
		// given
		User mockFollower = makeMockUser(1L, "follower");
		User mockFollowing = makeMockUser(2L, "following");
		given(userRepository.getReferenceById(mockFollower.getId())).willReturn(mockFollower);
		given(userRepository.findByUsername(mockFollowing.getUsername())).willReturn(Optional.of(mockFollowing));

		// when
		assertThatThrownBy(() -> followService.deleteFollow(mockFollower.getId(), mockFollowing.getUsername()))
				.isInstanceOf(FollowingException.class)
				.hasMessage("팔로잉 요청이 되어있지 않는 유저입니다");

		// then
		then(userRepository).should(times(1)).getReferenceById(anyLong());
		then(userRepository).should(times(1)).findByUsername(anyString());
	}

}
