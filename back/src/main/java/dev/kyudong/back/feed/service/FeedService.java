package dev.kyudong.back.feed.service;

import dev.kyudong.back.feed.api.dto.PostWithScore;
import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

	private final UserService userService;
	private final FollowRepository followRepository;
	private final PostUsecase postUsecase;
	private final RedissonClient redissonClient;
	private final ExecutorService feedExecutorService;

	@Transactional(readOnly = true)
	public FeedDetailResDto findFeedsByUser(Long userId, int page) {
		final String feedKey = "feed:user:" + userId;
		RList<String> feedCache = redissonClient.getList(feedKey);
		if (!feedCache.isExists()) {
			User user = userService.getUserProxy(userId);
			generateAndCacheFeedForUser(user, feedKey);
			List<Post> previewPosts = postUsecase.findPopularPostsWithUser(user, Instant.now(), 20);
			return FeedDetailResDto.of(true, 1, previewPosts);
		}

		return createFeedDetailRes(page, feedCache);
	}

	@Transactional(readOnly = true)
	public FeedDetailResDto findFeedsWithGuset(String guestId, int page) {
		if (!StringUtils.hasText(guestId)) {
			log.error("Guest Id가 없는 요청입니다");
			return FeedDetailResDto.empty();
		}

		final String feedKey = "feed:guest:" + guestId;
		RList<String> feedCache = redissonClient.getList(feedKey);
		if (!feedCache.isExists()) {
			generateAndCacheFeedForGuest(guestId, feedKey);
			List<Post> previewPosts = postUsecase.findPopularPostsWithGuest(Instant.now(), 20);
			return FeedDetailResDto.of(true, 1, previewPosts);
		}

		return createFeedDetailRes(page, feedCache);
	}

	private FeedDetailResDto createFeedDetailRes(int page, RList<String> feedCache) {
		int pageSize = 20;
		int startIndex = page * pageSize;
		int endIndex = startIndex + pageSize - 1;

		Set<Long> postIds = feedCache.range(startIndex, endIndex).stream()
				.map(Long::valueOf)
				.collect(Collectors.toSet());

		if (postIds.isEmpty()) {
			return FeedDetailResDto.empty();
		}

		List<Post> response = applyDiversityRules(postUsecase.findAllByIds(postIds));
		boolean hasNext = response.size() > (page + 1) + pageSize;
		Integer nextPage = hasNext ? page + 1 : null;

		return FeedDetailResDto.of(hasNext, nextPage, response);
	}

	/**
	 * 사용자의 피드 목록을 생성합니다
	 * @param user		사용자
	 * @param feedKey	저장될 키
	 */
	@Async
	protected void generateAndCacheFeedForUser(User user, String feedKey) {
		log.info("사용자 피드목록을 생성을 시작합니다: {}", feedKey);

		// 최신 게시글
		CompletableFuture<List<Post>> future1 = CompletableFuture.supplyAsync(() ->
				postUsecase.findRecentPostsWithUser(user, Instant.now().minus(48, ChronoUnit.HOURS), 50), feedExecutorService);

		// 인기가 많은 게시글
		CompletableFuture<List<Post>> future2 = CompletableFuture.supplyAsync(() ->
				postUsecase.findPopularPostsWithUser(user, Instant.now().minus(3, ChronoUnit.DAYS), 100), feedExecutorService);

		// 예전에 나온 게시글
		CompletableFuture<List<Post>> future3 = CompletableFuture.supplyAsync(() -> {
			Set<Long> randomPostIds = redissonClient.getSet("feed:random_post_ids");

			if (randomPostIds.isEmpty()) {
				return new ArrayList<>();
			}

			return postUsecase.findAllByIds(randomPostIds).stream()
					.filter(post -> !post.getUser().getId().equals(user.getId()))
					.toList();
		});

		// 팔로우 피드
		CompletableFuture<List<Post>> future4 = CompletableFuture.supplyAsync(() ->
				postUsecase.findByFollowingPost(user, Instant.now().minus(2, ChronoUnit.DAYS), 50), feedExecutorService);

		List<CompletableFuture<List<Post>>> futures = List.of(future1, future2, future3, future4);
		List<Long> followingList = followRepository.findByFollowingWithFollower(user).stream()
				.map(f -> f.getFollowing().getId())
				.toList();

		String seenKey = "feed_seen:user" + user.getId();
		saveCacheAndCalculatePostScore(futures, seenKey, feedKey, followingList, 30);
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
		CompletableFuture<List<Post>> future1 = CompletableFuture.supplyAsync(() ->
				postUsecase.findRecentPostsWithGuest(Instant.now().minus(48, ChronoUnit.HOURS), 50), feedExecutorService);

		// 인기가 많은 게시글
		CompletableFuture<List<Post>> future2 = CompletableFuture.supplyAsync(() ->
				postUsecase.findPopularPostsWithGuest(Instant.now().minus(3, ChronoUnit.DAYS), 100), feedExecutorService);

		// 예전에 나온 게시글
		CompletableFuture<List<Post>> future3 = CompletableFuture.supplyAsync(() -> {
			Set<Long> randomPostIds = redissonClient.getSet("feed:random_post_ids");

			if (randomPostIds.isEmpty()) {
				return new ArrayList<>();
			}

			return postUsecase.findAllByIds(randomPostIds);
		});

		List<CompletableFuture<List<Post>>> futures = List.of(future1, future2, future3);

		String seenKey = "feed_seen:guest" + guestId;
		saveCacheAndCalculatePostScore(futures, seenKey, feedKey, Collections.emptyList(), 60);
	}

	private void saveCacheAndCalculatePostScore(
			List<CompletableFuture<List<Post>>> futures,
			String seenKey,
			String feedKey,
			List<Long> followingList,
			int minutes
	) {
		RBloomFilter<Long> seenFilter = redissonClient.getBloomFilter(seenKey);
		List<PostWithScore> candidates = futures.stream()
				.flatMap(f -> f.join().stream())
				.distinct()
				.filter(post -> !seenFilter.contains(post.getId()))
				.map(post -> new PostWithScore(post, calculatePostScore(post, followingList)))
				.sorted(Comparator.comparingDouble(PostWithScore::postScore).reversed())
				.toList();

		List<String> cachePostIds =  candidates.stream()
				.map(postWithScore -> String.valueOf(postWithScore.post().getId()))
				.toList();
		RList<String> feedCache = redissonClient.getList(feedKey);
		feedCache.clear();
		feedCache.addAll(cachePostIds);
		feedCache.expire(Duration.ofMinutes(minutes));
	}

	/**
	 * 게시글의 총점수를 계산합니다
	 * @param post         	게시글
	 * @param followingIds	팔로우 목록
	 * @return 계산된 점수
	 */
	private double calculatePostScore(Post post, List<Long> followingIds) {
		log.debug("점수 계산을 시작합니다: postId={}", post.getId());

		double postScore = post.getPostScore();
		double popularityScore = (post.getPostViewCount() * 0.1) + postScore + (post.getCommentList().size() * 1.2) + 1;

		long hoursSinceCreation = ChronoUnit.HOURS.between(post.getCreatedAt(), Instant.now());
		double recencyWeight = Math.max(0.1, 1.0 - (hoursSinceCreation / (24.0 * 7)));

		double personalizationWeight = 1.0;
		if (followingIds.contains(post.getUser().getId())) {
			personalizationWeight = 5.0;
		}

		return (popularityScore * recencyWeight) * personalizationWeight;
	}

	/**
	 * 같은 작성자의 글이 중복되지 않게 제거 후 20개의 피드를 반환합니다
	 * @param posts 점수 계산이 된 게시글
	 * @return 반환될 게시글
	 */
	private List<Post> applyDiversityRules(List<Post> posts) {
		List<Post> finalFeed = new ArrayList<>();
		Map<Long, Integer> authorCountMap = new HashMap<>();
		final int MAX_POSTS_PER_AUTHOR = 2;

		for (Post post : posts) {
			if (finalFeed.size() >= 20) {
				break;
			}

			Long authorId = post.getUser().getId();

			int currentCount = authorCountMap.getOrDefault(authorId, 0);
			if (currentCount < MAX_POSTS_PER_AUTHOR) {
				finalFeed.add(post);
				authorCountMap.put(authorId, currentCount + 1);
			}
		}
		return finalFeed;
	}

}
