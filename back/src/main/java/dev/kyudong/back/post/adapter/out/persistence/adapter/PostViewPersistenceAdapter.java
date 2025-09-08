package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.repository.PostViewRepository;
import dev.kyudong.back.post.application.port.out.web.PostViewPersistencePort;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostView;
import dev.kyudong.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostViewPersistenceAdapter implements PostViewPersistencePort {

	private final PostViewRepository postViewRepository;

	@Override
	@Transactional
	public void increaseViewCount(User user, Post post) {
		postViewRepository.findByUserAndPost(user, post)
				.orElseGet(() -> postViewRepository.save(PostView.of(user, post)));
	}

}
