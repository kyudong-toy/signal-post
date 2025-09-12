package dev.kyudong.back.post.adapter.out.persistence.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.kyudong.back.post.application.port.out.web.CommentQueryPort;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.QComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryAdapter implements CommentQueryPort {

	private final JPAQueryFactory queryFactory;

	private static final QComment c = QComment.comment;

	@Override
	public List<Comment> findByOldCommentByCursor(Long postId, Long cursorId) {
		return queryFactory
				.selectFrom(c)
				.where(
						c.post.id.eq(postId),
						cursorId == null ? null : c.id.gt(cursorId)
				)
				.orderBy(c.createdAt.asc())
				.limit(21)
				.fetch();
	}

	@Override
	public List<Comment> findByNewCommentByCursor(Long postId, Long cursorId) {
		return queryFactory
				.selectFrom(c)
				.where(
						c.post.id.eq(postId),
						cursorId == null ? null : c.id.lt(cursorId)
				)
				.orderBy(c.createdAt.desc())
				.limit(21)
				.fetch();
	}

}
