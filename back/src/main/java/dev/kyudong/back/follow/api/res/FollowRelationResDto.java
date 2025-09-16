package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;

public record FollowRelationResDto(
		Long id,
		Long followerId,
		Long followingId,
		FollowStatus status
) {
	public static FollowRelationResDto from(Follow follow) {
		return new FollowRelationResDto(
				follow.getId(),
				follow.getFollower().getId(),
				follow.getFollowing().getId(),
				follow.getStatus()
		);
	}
}
