package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.feed.api.dto.PostFeedDto;

public record FeedAuthor(
		long id,
		String username
) {
	public static FeedAuthor from(PostFeedDto dto) {
		return new FeedAuthor(
				dto.authorId(), dto.username()
		);
	}
}
