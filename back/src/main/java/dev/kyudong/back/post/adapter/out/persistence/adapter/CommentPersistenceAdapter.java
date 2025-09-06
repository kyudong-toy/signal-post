package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.exception.CommentNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.repository.CommentRepository;
import dev.kyudong.back.post.application.port.out.CommentPersistencePort;
import dev.kyudong.back.post.domain.entity.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentPersistencePort {

	private final CommentRepository commentRepository;

	@Override
	public Comment save(Comment comment) {
		return commentRepository.save(comment);
	}

	@Override
	public Comment findByIdOrThrow(Long commentId) {
		return commentRepository.findById(commentId)
				.orElseThrow(() -> {
					log.warn("댓글 상태 수정 실패 - 존재하지 않는 댓글 : commentId: {}", commentId);
					return new CommentNotFoundException(commentId);
				});
	}

}
