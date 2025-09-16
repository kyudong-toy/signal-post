package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.post.application.port.out.web.PostPersistencePort;
import dev.kyudong.back.post.domain.entity.Post;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostPersistencePort {

	private final PostRepository postRepository;
	private final RedissonClient redissonClient;

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
	@Transactional(readOnly = true)
	public boolean existsById(Long postId) {
		return postRepository.existsById(postId);
	}

	@Override
	@Transactional
	public void refreshRandomOldPost() {
		Tuple maxAndMin = postRepository.findMaxAndMin();
		Long max = Optional.ofNullable(maxAndMin.get("max", Long.class)).orElse(0L);
		Long min = Optional.of(maxAndMin.get("min", Long.class)).orElse(0L);

		if (max == 0L || min == 0L || min >= max) {
			log.debug("게시물가 부족하여 추출을 중지합니다: max={}, min={}", max, min);
			return;
		}

		int sampleSize = 1000;
		Set<Long> randomIds = ThreadLocalRandom.current()
				.longs(min, max + 1)
				.distinct()
				.limit((long) (sampleSize * 1.3))
				.boxed()
				.collect(Collectors.toSet());
		Pageable pageable = PageRequest.of(0, sampleSize);

		Instant date = Instant.now().minus(6, ChronoUnit.MONTHS);
		List<Long> existingIds = postRepository.findIdsByIdIn(randomIds, date, pageable);
		if (existingIds.isEmpty()) {
			log.debug("게시글을 찾을 수 없어 생성을 중단합니다");
			return;
		}

		String cacheKey = "feed:random_post_ids";
		RSet<Long> cache = redissonClient.getSet(cacheKey);
		cache.clear();
		cache.addAll(existingIds);
		cache.expire(Duration.ofHours(4));
	}

}
