package dev.kyudong.back.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.handler.NotificationWebSocketHandler;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.utils.RedirectUrlCreator;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.dto.event.PostCreateNotification;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

public class NotificationEventHandlerTests extends UnitTestBase {

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

	@Mock
	private PostUsecase postUsecase;

	@Mock
	private UserReaderService userReaderService;

	@Test
	@DisplayName("게시글 생성 알림 이벤트 - 성공")
	void handlePostCreateEvent_success() throws JsonProcessingException {
		// given
		User mockFollowing = createMockUser("cnzn1d", 999L);
		given(userReaderService.getUserReference(mockFollowing.getId())).willReturn(mockFollowing);

		User mockFollower1 = createMockUser("ckzxv2", 1L);
		User mockFollower2 = createMockUser("ccxvn1sd", 2L);
		User mockFollower3 = createMockUser("ckj31zz", 3L);
		List<Follow> mockFollows = List.of(
				createMockFollow(mockFollower1, mockFollowing, 1L),
				createMockFollow(mockFollower2, mockFollowing, 2L),
				createMockFollow(mockFollower3, mockFollowing, 3L)
		);
		given(followRepository.findByFollowingWithFollower(mockFollowing)).willReturn(mockFollows);

		Post mockPost = createMockPost(mockFollowing);
		given(postUsecase.getPostEntityOrThrow(anyLong())).willReturn(mockPost);
		PostCreateNotification event = new PostCreateNotification(mockPost.getId(), mockFollowing.getId());

		given(redirectUrlCreator.createPostUrl(mockPost.getId())).willReturn("/ex");
		given(notificationRepository.saveAll(anyList())).willAnswer(invocation -> {
			List<Notification> notifications = invocation.getArgument(0);
			long tempId = 1L;
			for (Notification n : notifications) {
				ReflectionTestUtils.setField(n, "id", tempId++);
				ReflectionTestUtils.setField(n, "createdAt", Instant.now());
				ReflectionTestUtils.setField(n, "post", mockPost);
			}
			return notifications;
		});

		// when
		defaultNotificationEventHandler.handlePostCreateEvent(event);

		// then
		then(followRepository).should().findByFollowingWithFollower(mockFollowing);
		then(notificationRepository).should().saveAll(anyList());
		then(notificationWebSocketHandler).should(times(mockFollows.size()))
				.sendNotificationToUser(anyLong(), any(NotificationDetailResDto.class));
	}

}
