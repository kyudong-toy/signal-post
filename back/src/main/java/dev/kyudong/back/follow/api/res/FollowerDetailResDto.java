package dev.kyudong.back.follow.api.res;

import dev.kyudong.back.follow.domain.Follow;

public record FollowerDetailResDto(

) {
	public static FollowerDetailResDto from(Follow follow) {
		return new FollowerDetailResDto(

		);
	}
}
