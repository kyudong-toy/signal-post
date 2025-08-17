package dev.kyudong.back.post.repository;

import dev.kyudong.back.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	boolean existsById(Long postId);

}
