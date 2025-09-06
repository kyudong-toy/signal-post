package dev.kyudong.back.feed;

import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.feed.service.FeedService;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private FeedRepository feedRepository;

	@InjectMocks
	private FeedService feedService;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static Post makeMockPost(String subject, String content, User mockUser) {
		Post mockPost = Post.of("제목", "", Category.builder().build());
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	@Test
	@DisplayName("피드 조회 - 성공: 커서 없이 조회하여 다음 피드가 없음")
	void findFeeds_success_withOutCursor() {
		// given
		User mockUser = makeMockUser("cnzn1d", 999L);

		User mockFollower1 = makeMockUser("cxfzf", 1L);
		Feed feed1 = Feed.builder()
				.user(mockFollower1)
				.post(makeMockPost("안녕!", "Hello World!", mockFollower1))
				.build();

		User mockFollower2 = makeMockUser("zxzz", 2L);
		Feed feed2 = Feed.builder()
				.user(mockFollower2)
				.post(makeMockPost("ㅋㅋㅋㅋ", "헬로 월드", mockFollower2))
				.build();

		User mockFollower3 = makeMockUser("sdifodsaf3", 3L);
		Feed feed3 = Feed.builder()
				.user(mockFollower3)
				.post(makeMockPost("ㅂㅂㅂ", "바이 월드", mockFollower3))
				.build();
		Slice<Feed> mockSliceFeed = new SliceImpl<>(List.of(feed1, feed2, feed3));

		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);
		given(feedRepository.findFeedByFollowerWithPost(mockUser, PageRequest.of(0, 10)))
				.willReturn(mockSliceFeed);

		// when
		FeedDetailResDto response = feedService.findFeeds(mockUser.getId(), null, 10);

		// then
		assertThat(response.content().size()).isEqualTo(3);
		assertThat(response.content().get(0).subject()).isEqualTo("안녕!");
		assertThat(response.content().get(2).content()).isEqualTo("바이 월드");
		assertThat(response.hasNext()).isFalse();
		assertThat(response.lastFeedId()).isNull();
		then(userRepository).should().getReferenceById(mockUser.getId());
		then(feedRepository).should().findFeedByFollowerWithPost(mockUser, PageRequest.of(0, 10));
	}

	@Test
	@DisplayName("피드 조회 - 성공: 커서 포함하여 다음 컨텐츠 로드")
	void findFeedsAfterFirst_success_withCursor() {
		// given
		User mockUser = makeMockUser("cnzn1d", 999L);

		User mockFollower = makeMockUser("cxfzf", 1L);
		Post mockPost = makeMockPost("안녕!", "Hello World!", mockFollower);
		Feed feed = Feed.builder()
				.user(mockFollower)
				.post(mockPost)
				.build();
		ReflectionTestUtils.setField(feed, "id", 10L);

		int size = 1;
		PageRequest pageRequest = PageRequest.of(0, size);
		Slice<Feed> mockSliceFeed = new SliceImpl<>(List.of(feed), pageRequest, true);
		final Long lastFeedId = 190L;

		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);
		given(feedRepository.findFeedByFollowerWithPost(mockUser, lastFeedId, pageRequest))
				.willReturn(mockSliceFeed);

		// when
		FeedDetailResDto response = feedService.findFeeds(mockUser.getId(), lastFeedId, size);

		// then
		assertThat(response.content().size()).isEqualTo(1);
		assertThat(response.content().get(0).subject()).isEqualTo("안녕!");
		assertThat(response.hasNext()).isTrue();
		assertThat(response.lastFeedId()).isEqualTo(mockPost.getId());
		then(userRepository).should().getReferenceById(mockUser.getId());
		then(feedRepository).should().findFeedByFollowerWithPost(mockUser, lastFeedId, pageRequest);
	}

}
