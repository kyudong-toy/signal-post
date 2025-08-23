package dev.kyudong.back.feed;

import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.event.DefaulteedEventHandler;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.api.dto.event.PostCreateFeedEvent;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedEventHandlerTests {

	@Mock
	private FollowRepository followRepository;

	@Mock
	private FeedRepository feedRepository;

	// 빠른 테스트를 위해 구현체로 테스트
	@InjectMocks
	private DefaulteedEventHandler defaulteedEventHandler;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static Post makeMockPost(User mockUser) {
		Post mockPost = Post.builder()
				.subject("subject")
				.content("content")
				.build();
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	private static Follow makeMockFollow(User follower, User following, Long id) {
		Follow follow = Follow.builder()
				.follower(follower)
				.following(following)
				.build();
		ReflectionTestUtils.setField(follow, "id", id);
		return follow;
	}

	@Test
	@DisplayName("피드 생성 이벤트 - 성공")
	void handlePostCreateEvent_success() {
		// given
		User mockFollowing = makeMockUser("cnzn1d", 999L);
		Post mockPost = makeMockPost(mockFollowing);
		User mockFollower1 = makeMockUser("ckzxv2", 1L);
		User mockFollower2 = makeMockUser("ccxvn1sd", 2L);
		User mockFollower3 = makeMockUser("ckj31zz", 3L);

		Follow follow1 = makeMockFollow(mockFollower1, mockFollowing, 1L);
		Follow follow2 = makeMockFollow(mockFollower2, mockFollowing, 2L);
		Follow follow3 = makeMockFollow(mockFollower3, mockFollowing, 3L);
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
