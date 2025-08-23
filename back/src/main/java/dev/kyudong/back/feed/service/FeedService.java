package dev.kyudong.back.feed.service;

import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

	private final UserRepository userRepository;
	private final FeedRepository feedRepository;

	@Transactional(readOnly = true)
	public FeedDetailResDto findFeeds(final Long userId, Long lastFeedId, int size) {
		User user = userRepository.getReferenceById(userId);
		PageRequest pageRequest = PageRequest.of(0, size);

		Slice<Feed> feeds = (lastFeedId == null)
				? feedRepository.findFeedByFollowerWithPost(user, pageRequest)
				: feedRepository.findFeedByFollowerWithPost(user, lastFeedId, pageRequest);

		return FeedDetailResDto.from(feeds);
	}

}
