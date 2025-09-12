package dev.kyudong.back.post.adapter.out.persistence.adapter;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.kyudong.back.feed.api.dto.PostFeedDto;
import dev.kyudong.back.follow.domain.QFollow;
import dev.kyudong.back.post.application.port.out.web.PostFeedQueryPort;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.post.domain.entity.QComment;
import dev.kyudong.back.post.domain.entity.QPost;
import dev.kyudong.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFeedQueryAdapter implements PostFeedQueryPort {

	private final JPAQueryFactory queryFactory;

	private static final QPost p = QPost.post;
	private static final QComment c = QComment.comment;
	private static final QFollow f = QFollow.follow;

	private static final Expression<PostFeedDto> POST_FEED_DTO_PROJECTION = Projections.constructor(
			PostFeedDto.class,
			p.id,
			p.user.id,
			p.user.username,
			p.subject,
			p.content,
			p.status,
			p.viewCount,
			c.id.countDistinct(),
			p.score,
			p.createdAt,
			p.modifiedAt
	);

	private Expression<?>[] getGroupByFields() {
		return new Expression<?>[] {
				p.id,
				p.user.id,
				p.user.username,
				p.subject,
				p.content,
				p.status,
				p.viewCount,
				p.score,
				p.createdAt,
				p.modifiedAt
		};
	}

	@Override
	public List<PostFeedDto> findPreviewPosts(Long userId, int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.user.id.ne(userId),
						p.status.eq(PostStatus.NORMAL)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.createdAt.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findPreviewPosts(int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(p.status.eq(PostStatus.NORMAL))
				.groupBy(getGroupByFields())
				.orderBy(p.createdAt.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findRecentPosts(Long userId, Instant now, int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.user.id.ne(userId),
						p.status.eq(PostStatus.NORMAL),
						p.createdAt.goe(now)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.createdAt.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findRecentPosts(Instant now, int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.status.eq(PostStatus.NORMAL),
						p.createdAt.goe(now)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.createdAt.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findPopularPosts(Long userId, Instant now, int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.user.id.ne(userId),
						p.status.eq(PostStatus.NORMAL),
						p.createdAt.lt(now)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.score.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findPopularPosts(Instant now, int size) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.status.eq(PostStatus.NORMAL),
						p.createdAt.lt(now)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.score.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findByFollowingPost(Long userId, int size) {
		JPQLQuery<User> followingSubquery = JPAExpressions
				.select(f.following)
				.from(f)
				.where(f.follower.id.eq(userId));

		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(p.user.in(followingSubquery))
				.groupBy(getGroupByFields())
				.orderBy(p.score.desc(), p.createdAt.desc())
				.limit(size)
				.fetch();
	}

	@Override
	public List<PostFeedDto> findAllByIds(Long userId, Set<Long> postIds) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.user.id.ne(userId),
						p.status.eq(PostStatus.NORMAL),
						p.id.in(postIds)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.score.desc())
				.fetch();
	}

	@Override
	public List<PostFeedDto> findAllByIds(Set<Long> postIds) {
		return queryFactory
				.select(POST_FEED_DTO_PROJECTION)
				.from(p)
				.leftJoin(c).on(c.post.eq(p))
				.where(
						p.status.eq(PostStatus.NORMAL),
						p.id.in(postIds)
				)
				.groupBy(getGroupByFields())
				.orderBy(p.score.desc())
				.fetch();
	}

}
