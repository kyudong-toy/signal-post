package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
