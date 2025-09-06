package dev.kyudong.back.feed.event;

import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.domain.dto.event.PostCreateFeedEvent;
import dev.kyudong.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaulteedEventHandler implements FeedEventHandler {

	private final FeedRepository feedRepository;
	private final FollowRepository followRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handlePostCreate(PostCreateFeedEvent evnet) {
		log.info("게시글 생성 이벤트 수신완료: postId={}", evnet.post().getId());

		List<User> followers = followRepository.findByFollowingWithFollower(evnet.author()).stream()
				.map(Follow::getFollower)
				.toList();

		if (followers.isEmpty()) {
			log.debug("팔로워가 없어 이벤트를 종료합니다: authorId={}", evnet.author().getId());
			return;
		}

		int size = followers.size();
		log.debug("{}명에게 피드를 배달합니다: postId={}", size, evnet.post().getId());

		List<Feed> newFeeds = followers.stream()
				.map(follower ->
						Feed.builder()
						.user(follower)
						.post(evnet.post())
						.build()
				)
				.toList();

		feedRepository.saveAll(newFeeds);
		log.info("{}명에게 피드가 배달 되었습니다: postId={}", size, evnet.post().getId());
	}

}
