package dev.kyudong.back.post.application.port.out;

import dev.kyudong.back.post.domain.entity.Post;

public interface PostPersistencePort {

	Post findByIdOrThrow(Long postId);

	Post save(Post post);

	boolean existsById(Long postId);

}
