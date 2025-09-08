package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

	boolean existsById(Long postId);

	@Query("""
		SELECT p
		FROM Post p
		WHERE p.user != :user
		AND p.status = 'NORMAL'
		ORDER BY p.createdAt DESC
	""")
	List<Post> findRecentPostsWithUser(@Param("user") User user, @Param("now") Instant now, Pageable pageable);

	@Query("""
		SELECT p
		FROM Post p
		WHERE p.status = 'NORMAL'
		ORDER BY p.createdAt DESC
	""")
	List<Post> findRecentPostsWithGuest(@Param("now") Instant now, Pageable pageable);

	@Query("""
		SELECT p
		FROM Post p
		WHERE p.user != :user
		AND p.createdAt < :now
		AND p.status = 'NORMAL'
		ORDER BY p.postScore DESC
	""")
	List<Post> findPopularPostsWithUser(@Param("user") User user, @Param("now") Instant now, Pageable pageable);

	@Query("""
		SELECT p
		FROM Post p
		WHERE p.createdAt < :now
		AND p.status = 'NORMAL'
		ORDER BY p.postScore DESC
	""")
	List<Post> findPopularPostsWithGuest(@Param("now") Instant now, Pageable pageable);

	@Query("""
			SELECT p
			FROM Post p
			WHERE p.user IN (
				SELECT f.following
				FROM Follow f
				WHERE f.follower = :user
			)
			AND p.createdAt < :now
			ORDER BY p.postScore DESC, p.createdAt DESC
	""")
	List<Post> findByFollowingPost(
			@Param("user") User user,
			@Param("now") Instant now,
			Pageable pageable
	);

	@Query("SELECT MAX(p.id) AS max, MIN(p.id) AS min FROM Post p")
	Tuple findMaxAndMin();

	@Query("SELECT p.id FROM Post p WHERE p.id IN :ids")
	List<Long> findIdsByIdIn(@Param("ids") Set<Long> ids, Pageable pageable);


}
