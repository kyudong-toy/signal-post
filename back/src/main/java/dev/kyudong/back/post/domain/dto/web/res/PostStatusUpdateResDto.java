package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostStatus;

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
