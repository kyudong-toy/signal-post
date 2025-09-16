package dev.kyudong.back.follow.service;

import dev.kyudong.back.follow.api.res.*;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.exception.FollowingException;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
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
	private final UserReaderService userReaderService;

	@Transactional(readOnly = true)
	public List<FollowerDetailResDto> findFollowers(final String followerUsername) {
		log.debug("팔로워 목록 조회 시작: followerUsername={}", followerUsername);

		User follower = userReaderService.getUserByUsername(followerUsername);
		List<Follow> follows = followRepository.findByFollowing(follower);

		log.debug("팔로워 목록 조회 되었습니다: followerUsername={}, followerId={}", followerUsername, follower.getId());
		return follows.stream()
				.map(FollowerDetailResDto::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FollowingDetailResDto> findFollowings(final String followingUsername) {
		log.debug("팔로잉 목록 조회 시작: followingUsername={}", followingUsername);

		User following = userReaderService.getUserByUsername(followingUsername);
		List<Follow> follows = followRepository.findByFollower(following);

		log.debug("팔로잉 목록 조회 되었습니다: followingUsername={}, followingId={}", followingUsername, following.getId());
		return follows.stream()
				.map(FollowingDetailResDto::from)
				.toList();
	}

	@Transactional
	public FollowCreateResDto requestFollow(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 요청 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		User follower = userReaderService.getUserReference(followerUserId);
		User following = userReaderService.getUserByUsername(followingUsername);

		validateNotSelfFollow(follower.getId(), following.getId());

		if (followRepository.existsByFollowerAndFollowing(follower, following)) {
			log.warn("이미 팔로잉 요청 상태입니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, following.getId());
			throw new AlreadyFollowException(followerUserId, followingUsername);
		}

		Follow newFollow = Follow.create(follower, following);
		Follow savedFollow = followRepository.save(newFollow);

		log.debug("팔로잉 요청이 완료되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, following.getId());
		return FollowCreateResDto.from(savedFollow);
	}

	@Transactional
	public FollowRelationResDto accept(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 승낙 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.PENDING);
		follow.accept();

		log.debug("팔로잉 요청이 승낙되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowRelationResDto.from(follow);
	}

	@Transactional
	public FollowRelationResDto block(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 차단 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.FOLLOWING);
		follow.block();

		log.debug("팔로잉 차단이 되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowRelationResDto.from(follow);
	}

	@Transactional
	public FollowRelationResDto reject(final Long followerUserId, final String followingUsername) {
		log.debug("팔로잉 거절 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.PENDING);
		follow.reject();
		followRepository.delete(follow);

		log.debug("팔로잉이 거절 되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowRelationResDto.from(follow);
	}

	@Transactional
	public FollowRelationResDto unFollow(final Long followerUserId, final String followingUsername) {
		log.debug("언팔로잉 요청 시작: followerUserId={}, followingUsername={}", followerUserId, followingUsername);

		Follow follow = findActiveFollowOrThrow(followerUserId, followingUsername, FollowStatus.FOLLOWING);
		follow.unFollow();
		followRepository.delete(follow);

		log.debug("언팔로잉 요청이 완료되었습니다: followerUserId={}, followingUsername={}, followingUserId={}", followerUserId, followingUsername, follow.getFollowing().getId());
		return FollowRelationResDto.from(follow);
	}

	private void validateNotSelfFollow(Long followerUserId, Long followingUserId) {
		if (followerUserId.equals(followingUserId)) {
			throw new FollowingException("자기 자신을 팔로잉 할 수 없습니다");
		}
	}

	private Follow findActiveFollowOrThrow(final Long followingUserId, final String followerUsername, final FollowStatus status) {
		User follower = userReaderService.getUserByUsername(followerUsername);
		User following = userReaderService.getUserReference(followingUserId);

		Follow follow = followRepository.findByFollowerAndFollowingAndStatus(follower, following, status)
				.orElseThrow(() -> {
					log.warn("팔로잉 요청이 되어있지 않는 유저입니다: followerUserId={}, followingUserId={}", followingUserId, following.getId());
					return new FollowingException("팔로잉 요청이 되어있지 않는 유저입니다");
				});

		validateNotSelfFollow(followingUserId, follower.getId());

		return follow;
	}

}
