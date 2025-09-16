package dev.kyudong.back.feed.service;

import dev.kyudong.back.feed.api.dto.PostFeedDto;
import dev.kyudong.back.feed.api.dto.res.FeedListResDto;
import dev.kyudong.back.post.application.port.out.web.PostFeedQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

	private final PostFeedQueryPort postFeedQueryPort;
	private final RedissonClient redissonClient;
	private final FeedGenerator feedGenerator;

	public FeedListResDto findFeedsWithUser(Long userId, int page) {
		final String feedKey = "feed:user:" + userId;
		RList<String> feedCache = redissonClient.getList(feedKey);
		if (!feedCache.isExists()) {
			feedGenerator.generateAndCacheFeedForUser(userId, feedKey);
			List<PostFeedDto> previewPosts = postFeedQueryPort.findPreviewPosts(userId, 21);
			boolean hasNext = previewPosts.size() == 20;
			return FeedListResDto.of(hasNext, 1, previewPosts);
		}

		return createFeedDetailRes(page, feedCache);
	}

	public FeedListResDto findFeedsWithGuset(String guestId, int page) {
		if (!StringUtils.hasText(guestId)) {
			log.error("Guest Id가 없는 요청입니다");
			return FeedListResDto.empty();
		}

		final String feedKey = "feed:guest:" + guestId;
		RList<String> feedCache = redissonClient.getList(feedKey);
		if (!feedCache.isExists()) {
			feedGenerator.generateAndCacheFeedForGuest(guestId, feedKey);
			List<PostFeedDto> previewPosts = postFeedQueryPort.findPreviewPosts(21);
			boolean hasNext = previewPosts.size() == 20;
			return FeedListResDto.of(hasNext, 1, previewPosts);
		}

		return createFeedDetailRes(page, feedCache);
	}

	private FeedListResDto createFeedDetailRes(int page, RList<String> feedCache) {
		int pageSize = 20;
		int startIndex = page * pageSize;
		int endIndex = startIndex + pageSize - 1;

		Set<Long> postIds = feedCache.range(startIndex, endIndex).stream()
				.map(Long::valueOf)
				.collect(Collectors.toSet());

		if (postIds.isEmpty()) {
			return FeedListResDto.empty();
		}

		List<PostFeedDto> response = applyDiversityRules(postFeedQueryPort.findAllByIds(postIds));
		boolean hasNext = response.size() > (page + 1) + pageSize;
		Integer nextPage = hasNext ? page + 1 : null;

		return FeedListResDto.of(hasNext, nextPage, response);
	}

	/**
	 * 같은 작성자의 글이 중복되지 않게 제거 후 20개의 피드를 반환합니다
	 * @param postFeedDtos 점수 계산이 된 게시글
	 * @return 반환될 게시글
	 */
	private List<PostFeedDto> applyDiversityRules(List<PostFeedDto> postFeedDtos) {
		List<PostFeedDto> finalFeed = new ArrayList<>();
		Map<Long, Integer> authorCountMap = new HashMap<>();
		final int MAX_POSTS_PER_AUTHOR = 2;

		for (PostFeedDto dto : postFeedDtos) {
			if (finalFeed.size() >= 20) {
				break;
			}

			Long authorId = dto.authorId();

			int currentCount = authorCountMap.getOrDefault(authorId, 0);
			if (currentCount < MAX_POSTS_PER_AUTHOR) {
				finalFeed.add(dto);
				authorCountMap.put(authorId, currentCount + 1);
			}
		}
		return finalFeed;
	}

}
