package dev.kyudong.back.notification.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.domain.QNotification;
import dev.kyudong.back.post.domain.entity.QComment;
import dev.kyudong.back.post.domain.entity.QPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQuery {

	private final JPAQueryFactory queryFactory;

	private static final QNotification n = QNotification.notification;
	private static final QPost p = QPost.post;
	private static final QComment c = QComment.comment;

	public List<NotificationQueryDto> findNotifications(Long userId, Long cursorId) {
		return queryFactory
				.select(Projections.constructor(
						NotificationQueryDto.class,
						n.id,
						n.receiver.id,
						n.type,
						n.redirectUrl,
						n.createdAt,
						n.sender.id,
						n.sender.username,
						p.id,
						p.subject,
						p.content,
						c.id,
						c.content
				))
				.from(n)
				.innerJoin(n.sender)
				.leftJoin(n.post, p)
				.leftJoin(n.comment, c)
				.where(
						n.receiver.id.eq(userId),
						cursorId != null ? n.id.lt(cursorId) : null,
						n.isRead.isFalse()
				)
				.orderBy(n.id.desc())
				.limit(21)
				.fetch();
	}

}
