package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostView;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

	Optional<PostView> findByUserAndPost(User user, Post post);

}
