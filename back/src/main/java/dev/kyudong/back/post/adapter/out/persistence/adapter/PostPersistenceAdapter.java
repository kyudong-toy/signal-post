package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.post.application.port.out.PostPersistencePort;
import dev.kyudong.back.post.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostPersistencePort {

	private final PostRepository postRepository;

	@Override
	@Transactional(readOnly = true)
	public Post findByIdOrThrow(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> {
			log.warn("존재하지 않는 게시글입니다: postId={}", postId);
			return new PostNotFoundException(postId);
		});
	}

	@Override
	@Transactional
	public Post save(Post post) {
		return postRepository.save(post);
	}

	@Override
	public boolean existsById(Long postId) {
		return postRepository.existsById(postId);
	}

}
