package dev.kyudong.back.feed.service;

import dev.kyudong.back.feed.api.dto.ItemWithScore;
import dev.kyudong.back.feed.api.dto.PostFeedDto;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.application.port.out.web.PostFeedQueryPort;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedGenerator {

	private final PostFeedQueryPort postFeedQueryPort;
	private final FollowRepository followRepository;
	private final ExecutorService feedExecutorService;
	private final TransactionTemplate transactionTemplate;
	private final RedissonClient redissonClient;
	private final UserService userService;

	/**
	 * 사용자의 피드 목록을 생성합니다
	 * @param userId	사용자 고유 아이디
	 * @param feedKey	저장될 키
	 */
	@Async
	public void generateAndCacheFeedForUser(Long userId, String feedKey) {
		log.info("사용자 피드목록을 생성을 시작합니다: {}", feedKey);

		// 최신 게시글
		CompletableFuture<List<PostFeedDto>> future1 = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(action ->
						postFeedQueryPort.findRecentPostsWithUser(userId, Instant.now().minus(48, ChronoUnit.HOURS), 50)
				), feedExecutorService);

		// 인기가 많은 게시글
		CompletableFuture<List<PostFeedDto>> future2 = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(action ->
						postFeedQueryPort.findPopularPostsWithUser(userId, Instant.now(), 100)
				), feedExecutorService);

		// 예전에 나온 게시글
		CompletableFuture<List<PostFeedDto>> future3 = CompletableFuture.supplyAsync(() -> transactionTemplate.execute(action -> {
			Set<Long> randomPostIds = redissonClient.getSet("feed:random_post_ids");

			if (randomPostIds.isEmpty()) {
				return new ArrayList<>();
			}

			return postFeedQueryPort.findAllByIds(randomPostIds).stream()
					.filter(dto -> !dto.authorId().equals(userId))
					.toList();
		}), feedExecutorService);

		// 팔로우 피드
		CompletableFuture<List<PostFeedDto>> future4 = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(action ->
						postFeedQueryPort.findByFollowingPost(userId, Instant.now().minus(2, ChronoUnit.DAYS), 50)
				), feedExecutorService);

		CompletableFuture.allOf(future1, future2, future3, future4)
				.thenRun(() -> {
					String seenKey = "feed_seen:user" + userId;

					User user = userService.getUserProxy(userId);
					List<Long> followingList = followRepository.findByFollowingWithFollower(user).stream()
							.map(f -> f.getFollowing().getId())
							.toList();

					RBloomFilter<Long> seenFilter = redissonClient.getBloomFilter(seenKey);
					if (!seenFilter.isExists()) {
						seenFilter.tryInit(10_000, 0.01);
					}

					List<ItemWithScore> candidates = Stream.of(future1, future2, future3, future4)
							.flatMap(f -> f.join().stream())
							.distinct()
							.filter(dto -> !seenFilter.contains(dto.postId()))
							.map(dto -> new ItemWithScore(dto, calculatePostScore(dto, followingList)))
							.sorted(Comparator.comparingDouble(ItemWithScore::postScore).reversed())
							.toList();

					List<String> cachePostIds =  candidates.stream()
							.map(itemWithScore -> String.valueOf(itemWithScore.detailResDto().postId()))
							.toList();

					RList<String> feedCache = redissonClient.getList(feedKey);
					feedCache.clear();
					feedCache.addAll(cachePostIds);
					feedCache.expire(Duration.ofMinutes(30));
				})
				.exceptionally(throwable -> {
					log.error("사용자 피드 생성 중 오류가 발생했습니다", throwable);
					return null;
				});
	}

	/**
	 * 게스트의 피드 목록을 생성합니다
	 * @param guestId 게스트 아이디
	 * @param feedKey 저장될 키
	 */
	@Async
	protected void generateAndCacheFeedForGuest(String guestId, String feedKey) {
		log.info("게스트 피드목록을 생성을 시작합니다: {}", feedKey);

		// 최신 게시글
		CompletableFuture<List<PostFeedDto>> future1 = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(action ->
						postFeedQueryPort.findRecentPostsWithGuest(Instant.now().minus(48, ChronoUnit.HOURS), 50)
				), feedExecutorService);

		// 인기가 많은 게시글
		CompletableFuture<List<PostFeedDto>> future2 = CompletableFuture.supplyAsync(() ->
				transactionTemplate.execute(action ->
						postFeedQueryPort.findPopularPostsWithGuest(Instant.now().minus(3, ChronoUnit.DAYS), 100)
				), feedExecutorService);

		// 예전에 나온 게시글
		CompletableFuture<List<PostFeedDto>> future3 = CompletableFuture.supplyAsync(() -> transactionTemplate.execute(action -> {
			Set<Long> randomPostIds = redissonClient.getSet("feed:random_post_ids");

			if (randomPostIds.isEmpty()) {
				return new ArrayList<>();
			}

			return postFeedQueryPort.findAllByIds(randomPostIds);
		}), feedExecutorService);

		CompletableFuture.allOf(future1, future2, future3)
				.thenRun(() -> {
					String seenKey = "feed_seen:guest" + guestId;

					RBloomFilter<Long> seenFilter = redissonClient.getBloomFilter(seenKey);
					if (!seenFilter.isExists()) {
						seenFilter.tryInit(10_000, 0.01);
					}

					List<ItemWithScore> candidates = Stream.of(future1, future2, future3)
							.flatMap(f -> f.join().stream())
							.distinct()
							.filter(dto -> !seenFilter.contains(dto.postId()))
							.map(p -> new ItemWithScore(p, calculatePostScore(p, new ArrayList<>())))
							.sorted(Comparator.comparingDouble(ItemWithScore::postScore).reversed())
							.toList();

					List<String> cachePostIds =  candidates.stream()
							.map(itemWithScore -> String.valueOf(itemWithScore.detailResDto().postId()))
							.toList();

					RList<String> feedCache = redissonClient.getList(feedKey);
					feedCache.clear();
					feedCache.addAll(cachePostIds);
					feedCache.expire(Duration.ofMinutes(60));
				})
				.exceptionally(throwable -> {
					log.error("게스트 피드 생성 중 오류가 발생했습니다", throwable);
					return null;
				});
	}

	/**
	 * 게시글의 총점수를 계산합니다
	 * @param postFeedDto   게시글과 댓글 개수
	 * @param followingIds			팔로우 목록
	 * @return 계산된 점수
	 */
	private double calculatePostScore(PostFeedDto postFeedDto, List<Long> followingIds) {
		double postScore = postFeedDto.score();
		double popularityScore = (postFeedDto.viewCount() * 0.1) + postScore + (postFeedDto.commentCount() * 1.2) + 1;

		long hoursSinceCreation = ChronoUnit.HOURS.between(postFeedDto.createdAt(), Instant.now());
		double recencyWeight = Math.max(0.1, 1.0 - (hoursSinceCreation / (24.0 * 7)));

		double personalizationWeight = 1.0;
		if (followingIds.contains(postFeedDto.authorId())) {
			personalizationWeight = 5.0;
		}

		return (popularityScore * recencyWeight) * personalizationWeight;
	}

}
