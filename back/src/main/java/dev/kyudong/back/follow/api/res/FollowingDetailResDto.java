package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;

public record FollowingDetailResDto(

) {
	public static FollowingDetailResDto from(Follow follow) {
		return new FollowingDetailResDto(

		);
	}
}
