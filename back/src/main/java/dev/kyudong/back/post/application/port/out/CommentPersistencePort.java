package dev.kyudong.back.post.application.port.out;

import dev.kyudong.back.post.domain.entity.Comment;

public interface CommentPersistencePort {

	Comment save(Comment comment);

	Comment findByIdOrThrow(Long commentId);


}
