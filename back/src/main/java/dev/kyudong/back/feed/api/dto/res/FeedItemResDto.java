package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.feed.api.dto.PostFeedDto;

public record FeedItemResDto(
		FeedAuthor author,
		FeedContent content
) {
	public static FeedItemResDto from(PostFeedDto dto) {
		return new FeedItemResDto(
				FeedAuthor.from(dto), FeedContent.from(dto)
		);
	}
}
