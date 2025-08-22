package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;

public record FollowAcceptResDto(
		Long id,
		Long followerId,
		Long followingId,
		FollowStatus status
) {
	public static FollowAcceptResDto from(Follow follow) {
		return new FollowAcceptResDto(
				follow.getId(),
				follow.getFollower().getId(),
				follow.getFollowing().getId(),
				follow.getStatus()
		);
	}
}
