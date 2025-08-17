package dev.kyudong.back.post.api.dto.res;

import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.domain.PostStatus;

public record PostStatusUpdateResDto(
		long postId,
		PostStatus status
) {
	public static PostStatusUpdateResDto from(Post post) {
		return new PostStatusUpdateResDto(
				post.getId(), post.getStatus()
		);
	}
}
