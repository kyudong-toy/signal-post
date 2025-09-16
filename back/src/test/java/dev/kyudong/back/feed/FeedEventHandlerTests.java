package dev.kyudong.back.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.event.DefaulteedEventHandler;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.domain.dto.event.PostCreateFeedEvent;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class FeedEventHandlerTests extends UnitTestBase {

	@Mock
	private FollowRepository followRepository;

	@Mock
	private FeedRepository feedRepository;

	// 빠른 테스트를 위해 구현체로 테스트
	@InjectMocks
	private DefaulteedEventHandler defaulteedEventHandler;

	@Test
	@DisplayName("피드 생성 이벤트 - 성공")
	void handlePostCreateEvent_success() throws JsonProcessingException {
		// given
		User mockFollowing = createMockUser("cnzn1d", 999L);
		Post mockPost = createMockPost(mockFollowing);
		User mockFollower1 = createMockUser("ckzxv2", 1L);
		User mockFollower2 = createMockUser("ccxvn1sd", 2L);
		User mockFollower3 = createMockUser("ckj31zz", 3L);

		Follow follow1 = createMockFollow(mockFollower1, mockFollowing, 1L);
		Follow follow2 = createMockFollow(mockFollower2, mockFollowing, 2L);
		Follow follow3 = createMockFollow(mockFollower3, mockFollowing, 3L);
		List<Follow> mockFollows = List.of(follow1, follow2, follow3);

		PostCreateFeedEvent event = new PostCreateFeedEvent(mockPost, mockFollowing);

		given(followRepository.findByFollowingWithFollower(mockFollowing))
				.willReturn(mockFollows);

		ArgumentCaptor<List<Feed>> feedListCaptor = ArgumentCaptor.forClass(List.class);

		// when
		defaulteedEventHandler.handlePostCreate(event);

		// then
		verify(feedRepository).saveAll(feedListCaptor.capture());
		List<Feed> capturedFeeds = feedListCaptor.getValue();

		assertThat(capturedFeeds.size()).isEqualTo(3);
		List<Long> capturedUserIds = capturedFeeds.stream()
				.map(feed -> feed.getUser().getId())
				.toList();
		assertThat(capturedUserIds).containsExactlyInAnyOrder(1L, 2L, 3L);
	}

}
