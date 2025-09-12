package dev.kyudong.back.post.domain.dto.web.res;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record CommentItemResDto(
		CommentAuthor author,
		CommentContent content
) {
	public static CommentItemResDto from(Comment comment) {
		return new CommentItemResDto(CommentAuthor.from(comment), CommentContent.from(comment));
	}
}
