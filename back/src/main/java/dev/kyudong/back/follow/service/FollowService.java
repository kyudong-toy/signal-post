package dev.kyudong.back.follow.service;

import dev.kyudong.back.follow.api.res.*;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.exception.FollowingException;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

	private final FollowRepository followRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<FollowerDetailResDto> findFollowers(final String followerUsername) {
		log.debug("팔로워 목록 조회 시작: followerUsername={}", followerUsername);

		User follower = userRepository.findByUsername(followerUsername)
				.orElseThrow(() -> {
					log.warn("팔로잉할 사용자가 조회되지 않았습니다: followerUsername={}", followerUsername);
					return new UserNotFoundException(followerUsername);
				});
		List<Follow> follows = followRepository.findByFollowing(follower);

		log.info("팔로워 목록 조회 되었습니다: followerUsername={}, followerId={}", followerUsername, follower.getId());
		return follows.stream()
				.map(FollowerDetailResDto::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FollowingDetailResDto> findFollowings(final String followingUsername) {
		log.debug("팔로잉 목록 조회 시작: followingUsername={}", followingUsername);

		User following = userRepository.findByUsername(followingUsername)
				.orElseThrow(() -> {
					log.warn("팔로잉할 사용자가 조회되지 않았습니다: followingUsername={}", followingUsername);
					return new UserNotFoundException(followingUsername);
				});
		List<Follow> follows = followRepository.findByFollower(following);

		log.info("팔로잉 목록 조회 되었습니다: followingUsername={}, followingId={}", followingUsername, following.getId());
		return follows.stream()
				.map(FollowingDetailResDto::from)
				.toList();
	}

	@Transactional
	public FollowCreateResDto createFollow(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 요청 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		User follower = userRepository.getReferenceById(followerUserId);
		User following = userRepository.findByUsername(followingUsername)
				.orElseThrow(() -> {
					log.warn("팔로잉할 사용자가 조회되지 않았습니다: username={}", followingUsername);
					return new UserNotFoundException(followingUsername);
				});
		validateNotSelfFollow(followerUserId, following.getId());

		if (followRepository.existsByFollowerAndFollowing(follower, following)) {
			log.warn("이미 팔로잉된 상태입니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, following.getId());
			throw new AlreadyFollowException(followerUserId, followingUsername);
		}

		Follow newFollow = Follow.builder()
				.follower(follower)
				.following(following)
				.build();

		Follow savedFollow = followRepository.save(newFollow);

		log.info("팔로잉 요청이 완료되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, following.getId());
		return FollowCreateResDto.from(savedFollow);
	}

	@Transactional
	public FollowAcceptResDto acceptFollow(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 승낙 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.PENDING);
		follow.accept();

		log.info("팔로잉 요청이 승낙되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowAcceptResDto.from(follow);
	}

	@Transactional
	public FollowBlokcedResDto blokcFollow(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 차단 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.FOLLOWING);
		follow.block();

		log.info("팔로잉 차단이 되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowBlokcedResDto.from(follow);
	}

	@Transactional
	public FollowDeleteResDto deleteFollow(final Long followerUserId, final String followingUsername) {
		log.debug("언팔로잉 요청 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.FOLLOWING);
		follow.unfollow();

		log.info("언팔로잉 요청이 완료되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowDeleteResDto.from(follow);
	}

	private void validateNotSelfFollow(Long followerUserId, Long followingUserId) {
		if (followerUserId.equals(followingUserId)) {
			throw new FollowingException("자기 자신을 팔로잉 할 수 없습니다");
		}
	}

	private Follow findActiveFollowOrThrow(final Long followingUserId, final String followerUsername, final FollowStatus status) {
		User follower = userRepository.findByUsername(followerUsername)
				.orElseThrow(() -> {
					log.warn("팔로잉할 사용자가 조회되지 않았습니다: followingUsername={}", followerUsername);
					return new UserNotFoundException(followerUsername);
				});
		User following = userRepository.getReferenceById(followingUserId);

		Follow follow = followRepository.findByFollowerAndFollowingAndStatus(follower, following, status)
				.orElseThrow(() -> {
					log.warn("팔로잉 요청이 되어있지 않는 유저입니다: followerUserId={}, followingUserId={}", followingUserId, following.getId());
					return new FollowingException("팔로잉 요청이 되어있지 않는 유저입니다");
				});
		validateNotSelfFollow(followingUserId, follower.getId());

		return follow;
	}

}
