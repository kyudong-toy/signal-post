package dev.kyudong.back.feed.api.dto.res;

import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.entity.Post;

import java.util.Collections;
import java.util.List;

public record FeedDetailResDto(
		boolean hasNext,
		Integer nextPage,
		List<PostDetailResDto> content
) {
	public static FeedDetailResDto empty() {
		return new FeedDetailResDto(false, 0, Collections.emptyList());
	}
	public static FeedDetailResDto of(boolean hasNext, Integer nextPage, List<Post> posts) {
		List<PostDetailResDto> content = posts.stream()
				.map(PostDetailResDto::from)
				.toList();
		return new FeedDetailResDto(hasNext, nextPage, content);
	}
}
