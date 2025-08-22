package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;

public record FollowDeleteResDto(
		Long followId,
		Long followerId,
		Long followingId,
		FollowStatus status
) {
	public static FollowDeleteResDto from(Follow follow) {
		return new FollowDeleteResDto(
				follow.getId(),
				follow.getFollower().getId(),
				follow.getFollowing().getId(),
				follow.getStatus()
		);
	}
}
