package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.feed.api.dto.PostFeedDto;

import java.util.Collections;
import java.util.List;

public record FeedListResDto(
		boolean hasNext,
		Integer nextPage,
		List<FeedItemResDto> content
) {
	public static FeedListResDto empty() {
		return new FeedListResDto(false, 0, Collections.emptyList());
	}
	public static FeedListResDto of(boolean hasNext, Integer nextPage, List<PostFeedDto> list) {
		List<FeedItemResDto> content = list.stream()
				.map(FeedItemResDto::from)
				.toList();
		return new FeedListResDto(hasNext, nextPage, content);
	}
}
