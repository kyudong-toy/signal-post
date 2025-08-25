package dev.kyudong.back.notification;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.event.DefaultNotificationEventHandler;
import dev.kyudong.back.notification.handler.NotificationWebSocketHandler;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.utils.RedirectUrlCreator;
import dev.kyudong.back.post.api.dto.event.PostCreateNotification;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class NotificationEventHandlerTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private FollowRepository followRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
	private DefaultNotificationEventHandler defaultNotificationEventHandler;

	@Mock
	private RedirectUrlCreator redirectUrlCreator;

	@Mock
	private NotificationWebSocketHandler notificationWebSocketHandler;

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
	@DisplayName("알림 생성 이벤트 - 성공")
	void handlePostCreateEvent_success() {
		// given
		User mockFollowing = makeMockUser("cnzn1d", 999L);
		given(userRepository.getReferenceById(mockFollowing.getId())).willReturn(mockFollowing);

		Post mockPost = makeMockPost(mockFollowing);
		User mockFollower1 = makeMockUser("ckzxv2", 1L);
		User mockFollower2 = makeMockUser("ccxvn1sd", 2L);
		User mockFollower3 = makeMockUser("ckj31zz", 3L);

		List<Follow> mockFollows = List.of(
				makeMockFollow(mockFollower1, mockFollowing, 1L),
				makeMockFollow(mockFollower2, mockFollowing, 2L),
				makeMockFollow(mockFollower3, mockFollowing, 3L)
		);
		PostCreateNotification event = new PostCreateNotification(mockPost.getId(), mockFollowing.getId());

		given(followRepository.findByFollowingWithFollower(mockFollowing)).willReturn(mockFollows);
		given(redirectUrlCreator.createPostUrl(mockPost.getId())).willReturn("/ex");
		given(notificationRepository.saveAll(anyList())).willAnswer(invocation -> {
			List<Notification> notifications = invocation.getArgument(0);
			long tempId = 1L;
			for (Notification n : notifications) {
				ReflectionTestUtils.setField(n, "id", tempId++);
				ReflectionTestUtils.setField(n, "createdAt", Instant.now());
			}
			return notifications;
		});

		// when
		defaultNotificationEventHandler.handlePostCreateEvent(event);

		// then
		then(followRepository).should().findByFollowingWithFollower(mockFollowing);
		then(notificationRepository).should().saveAll(anyList());
		then(notificationWebSocketHandler).should(times(mockFollows.size()))
				.sendMessageToUser(anyLong(), any(NotificationDetailResDto.class));
	}

}
