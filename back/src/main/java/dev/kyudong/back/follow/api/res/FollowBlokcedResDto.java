package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;

public record FollowBlokcedResDto(
		Long followId,
		Long followerId,
		Long followingId,
		FollowStatus status
) {
	public static FollowBlokcedResDto from(Follow follow) {
		return new FollowBlokcedResDto(
				follow.getId(),
				follow.getFollower().getId(),
				follow.getFollowing().getId(),
				follow.getStatus()
		);
	}
}
