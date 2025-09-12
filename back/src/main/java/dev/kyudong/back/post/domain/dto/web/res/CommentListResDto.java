package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;

import java.util.Collections;
import java.util.List;

public record CommentListResDto(
		boolean hasNext,
		Long cursorId,
		List<CommentItemResDto> comments
) {
	public static CommentListResDto empty() {
		return new CommentListResDto(false, 0L, Collections.emptyList());
	}
	public static CommentListResDto from(List<Comment> commentList) {
		boolean hasNext = commentList.size() > 20;

		List<CommentItemResDto> comments = commentList.stream()
				.map(CommentItemResDto::from)
				.limit(20)
				.toList();

		Long cursorId = commentList.get(comments.size() - 1).getId();
		return new CommentListResDto(hasNext, cursorId, comments);
	}
}
