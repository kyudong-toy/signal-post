package dev.kyudong.back.follow;

import dev.kyudong.back.follow.api.res.*;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.exception.FollowingException;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.follow.service.FollowService;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

public class FollowServiceTests extends UnitTestBase {

	@Mock
	private FollowRepository followRepository;

	@InjectMocks
	private FollowService followService;

	@Mock
	private UserReaderService userReaderService;

	@Nested
	@DisplayName("팔로잉 요청")
	class CreateFollow {

		@Test
		@DisplayName("팔로잉 요청 - 성공")
		void success() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);
			given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class)))
					.willReturn(false);

			Follow mockFollow = createMockFollow(mockFollower, mockFollowing, 1L);
			given(followRepository.save(any(Follow.class))).willReturn(mockFollow);

			// when
			FollowCreateResDto followCreateResDto = followService.requestFollow(
					mockFollower.getId(), mockFollowing.getUsername());

			// then
			assertThat(followCreateResDto.followerId()).isEqualTo(mockFollower.getId());
			assertThat(followCreateResDto.followingId()).isEqualTo(mockFollowing.getId());
			assertThat(followCreateResDto.status()).isEqualTo(FollowStatus.PENDING);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
			then(followRepository).should(times(1)).existsByFollowerAndFollowing(any(User.class), any(User.class));
			then(followRepository).should(times(1)).save(any(Follow.class));
		}

		@Test
		@DisplayName("팔로잉 요청 - 실패 : 자기 자신을 팔로잉 시도함")
		void fail_selfFollowing() {
			// given
			User mockFollower = createMockUser("Hello", 1L);
			User mockFollowing = createMockUser("Hello", 1L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			// when & then
			assertThatThrownBy(() -> followService.requestFollow(mockFollower.getId(), mockFollowing.getUsername()))
					.isInstanceOf(FollowingException.class)
					.hasMessage("자기 자신을 팔로잉 할 수 없습니다");
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
			then(followRepository).should(never()).existsByFollowerAndFollowing(any(User.class), any(User.class));
			then(followRepository).should(never()).save(any(Follow.class));
		}

		@Test
		@DisplayName("팔로잉 요청 - 실패 : 팔로잉 상대가 존재하지 않음")
		void createFollow_fail_followerNotFound() {
			// given
			User mockFollower = createMockUser("follower", 999L);
			User mockFollowing = createMockUser("ghost", 1L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			doThrow(new UserNotFoundException(mockFollowing.getUsername()))
					.when(userReaderService).getUserByUsername(mockFollowing.getUsername());

			// when & then
			assertThatThrownBy(() -> followService.requestFollow(mockFollower.getId(), mockFollowing.getUsername()))
					.isInstanceOf(UserNotFoundException.class)
					.hasMessageContaining(mockFollowing.getUsername());
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
			then(followRepository).should(never()).existsByFollowerAndFollowing(any(User.class), any(User.class));
			then(followRepository).should(never()).save(any(Follow.class));
		}

		@Test
		@DisplayName("팔로잉 요청 - 실패 : 이미 팔로잉 중임")
		void fail_alreadyFollowing() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);
			given(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).willReturn(true);

			// when & then
			assertThatThrownBy(() -> followService.requestFollow(mockFollower.getId(), mockFollowing.getUsername()))
					.isInstanceOf(AlreadyFollowException.class);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
			then(followRepository).should(times(1)).existsByFollowerAndFollowing(any(User.class), any(User.class));
			then(followRepository).should(never()).save(any(Follow.class));
		}

	}

	@Nested
	@DisplayName("팔로잉 응답")
	class FollowAction {

		@Test
		@DisplayName("팔로잉 승낙 요청 - 성공")
		void accept_success() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			Follow mockFollow = createMockFollow(mockFollower, mockFollowing, 1L);
			given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
					.willReturn(Optional.of(mockFollow));

			// when
			FollowRelationResDto response = followService.accept(mockFollower.getId(), mockFollowing.getUsername());

			// then
			assertThat(response.followerId()).isEqualTo(mockFollower.getId());
			assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
			assertThat(response.status()).isEqualTo(FollowStatus.FOLLOWING);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
		}

		@Test
		@DisplayName("팔로잉 차단 요청 - 성공")
		void blocked_success() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			Follow mockFollow = createMockFollow(mockFollower, mockFollowing, 1L);
			given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
					.willReturn(Optional.ofNullable(mockFollow));

			// when
			FollowRelationResDto response = followService.block(mockFollower.getId(), mockFollowing.getUsername());

			// then
			assertThat(response.followerId()).isEqualTo(mockFollower.getId());
			assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
			assertThat(response.status()).isEqualTo(FollowStatus.BLOCKED);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
		}

		@Test
		@DisplayName("팔로잉 거절 요청 - 성공")
		void reject_success() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			Follow mockFollow = createMockFollow(mockFollower, mockFollowing, 1L);
			given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
					.willReturn(Optional.ofNullable(mockFollow));

			// when
			FollowRelationResDto response = followService.reject(mockFollower.getId(), mockFollowing.getUsername());

			// then
			assertThat(response.followerId()).isEqualTo(mockFollower.getId());
			assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
			assertThat(response.status()).isEqualTo(FollowStatus.REJECT);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
		}

		@Test
		@DisplayName("팔로잉 취소 요청 - 성공")
		void unFollow_success() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			Follow mockFollow = createMockFollow(mockFollower, mockFollowing, 1L);
			given(followRepository.findByFollowerAndFollowingAndStatus(any(User.class), any(User.class), any(FollowStatus.class)))
					.willReturn(Optional.ofNullable(mockFollow));

			// when
			FollowRelationResDto response = followService.unFollow(mockFollower.getId(), mockFollowing.getUsername());

			// then
			assertThat(response.followerId()).isEqualTo(mockFollower.getId());
			assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
			assertThat(response.status()).isEqualTo(FollowStatus.UNFOLLOWED);
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
		}

		@Test
		@DisplayName("실패 : 팔로잉이 서로 안되있음")
		void deleteFollow_fail_notFollowing() {
			// given
			User mockFollower = createMockUser("follower", 1L);
			User mockFollowing = createMockUser("following", 2L);
			given(userReaderService.getUserReference(mockFollower.getId())).willReturn(mockFollower);
			given(userReaderService.getUserByUsername(mockFollowing.getUsername())).willReturn(mockFollowing);

			// when
			assertThatThrownBy(() -> followService.unFollow(mockFollower.getId(), mockFollowing.getUsername()))
					.isInstanceOf(FollowingException.class)
					.hasMessage("팔로잉 요청이 되어있지 않는 유저입니다");

			// then
			then(userReaderService).should(times(1)).getUserReference(anyLong());
			then(userReaderService).should(times(1)).getUserByUsername(anyString());
		}

	}

}
