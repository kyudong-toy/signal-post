package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

	boolean existsById(Long postId);

	@Query("""
        SELECT p
        FROM Post p
        JOIN FETCH Comment c
        WHERE p.id = c.post.id
        And p.user != :user
        AND p.user.id NOT IN (SELECT f.following.id FROM Follow f WHERE f.follower = :user)
        AND p.id NOT IN :excludedPostIds
        ORDER BY p.createdAt DESC
    """)
	List<Post> findDiscoveryPosts(
			@Param("user") User user,
			@Param("excludedPostIds") List<Long> excludedPostIds,
			Pageable pageable
	);

}
