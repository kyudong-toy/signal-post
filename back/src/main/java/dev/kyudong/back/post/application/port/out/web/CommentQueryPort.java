package dev.kyudong.back.post.application.port.out.web;

import dev.kyudong.back.post.domain.entity.Comment;

import java.util.List;

public interface CommentQueryPort {

	List<Comment> findByOldCommentByCursor(Long postId, Long cursorId);

	List<Comment> findByNewCommentByCursor(Long postId, Long cursorId);

}
