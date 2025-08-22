package dev.kyudong.back.feed.repository;

import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

	@Query("""
		SELECT f
		FROM Feed f
		JOIN FETCH f.post p
		JOIN FETCH p.user
		WHERE f.user = :user
		AND f.id < :lastFeedId
		ORDER BY f.id DESC, f.createdAt DESC
		""")
	Slice<Feed> findFeedByFollowerWithPost(@Param("user") User user, @Param("lastFeedId") Long lastFeedId, Pageable pageable);

	@Query("""
		SELECT f
		FROM Feed f
		JOIN FETCH f.post p
		JOIN FETCH p.user
		WHERE f.user = :user
		ORDER BY f.id DESC, f.createdAt DESC
		""")
	Slice<Feed> findFeedByFollowerWithPost(@Param("user") User user, Pageable pageable);

}
