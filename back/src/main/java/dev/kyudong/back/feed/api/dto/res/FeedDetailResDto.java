package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import org.springframework.data.domain.Slice;

import java.util.List;

public record FeedDetailResDto(
		Long lastFeedId,
		boolean hasNext,
		List<PostDetailResDto> content
) {
	public static FeedDetailResDto from(Slice<Feed> feeds) {
		List<PostDetailResDto> content = feeds.getContent().stream()
				.map(feed -> PostDetailResDto.from(feed.getPost()))
				.toList();

		Long lastFeedId = null;
		boolean hasNext = feeds.hasNext();

		if (!content.isEmpty() && hasNext) {
			lastFeedId = content.get(content.size() - 1).postId();
		}

		return new FeedDetailResDto(lastFeedId, hasNext, content);
	}
}
