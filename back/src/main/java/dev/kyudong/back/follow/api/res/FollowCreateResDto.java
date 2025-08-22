package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;

public record FollowCreateResDto(
		Long id,
		Long followerId,
		Long followingId,
		FollowStatus status
) {
	public static FollowCreateResDto from(Follow follow) {
		return new FollowCreateResDto(
				follow.getId(),
				follow.getFollower().getId(),
				follow.getFollowing().getId(),
				follow.getStatus()
		);
	}
}
