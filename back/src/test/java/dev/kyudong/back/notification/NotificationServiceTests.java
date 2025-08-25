package dev.kyudong.back.notification;

import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.notification.repository.NotificationRepository;
import dev.kyudong.back.notification.service.NotificationService;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks
	private NotificationService notificationService;

	private static User makeMockUser(String username, Long id) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", id);
		return mockUser;
	}

	private static Notification makeMockNotification(User receiver, User sender, String redirectUrl, Long id) {
		Notification mockNotification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl(redirectUrl)
				.type(NotificationType.POST)
				.build();
		ReflectionTestUtils.setField(mockNotification, "id", id);
		return mockNotification;
	}

	@Test
	@DisplayName("알림 목록 조회 - 성공: 첫 조회")
	void findNotification_success() {
		// given
		final Long receiverId = 1L;
		User mockReceivcer = makeMockUser("sdfdsz", receiverId);
		given(userRepository.getReferenceById(mockReceivcer.getId())).willReturn(mockReceivcer);
		User mockSender = makeMockUser("dkxzzz", 2L);

		PageRequest pageRequest = PageRequest.of(0, 10);
		Notification mockNotification1 = makeMockNotification(mockReceivcer, mockSender, "/post/1", 1L);
		Notification mockNotification2 = makeMockNotification(mockReceivcer, mockSender, "/post/2", 2L);
		Slice<Notification> mockSliceNotification = new SliceImpl<>(
				List.of(mockNotification1, mockNotification2), pageRequest, false
		);
		given(notificationRepository.findNotificationByReceiver(mockReceivcer, pageRequest))
				.willReturn(mockSliceNotification);

		// when
		NotificationResDto response = notificationService
				.findNotifications(receiverId, null, 10);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isEqualTo(false);
		assertThat(response.lastNotificationId()).isNull();
		assertThat(response.content().get(0).redirectUrl()).isEqualTo("/post/1");
		then(userRepository).should().getReferenceById(receiverId);
		then(notificationRepository).should().findNotificationByReceiver(mockReceivcer, pageRequest);
	}

	@Test
	@DisplayName("알림 목록 조회 - 성공: 커서를 이용한 조회")
	void findNotification_success_withCursor() {
		// given
		final Long receiverId = 1L;
		User mockReceivcer = makeMockUser("sdfdsz", receiverId);
		given(userRepository.getReferenceById(mockReceivcer.getId())).willReturn(mockReceivcer);
		User mockSender = makeMockUser("dkxzzz", 2L);

		PageRequest pageRequest = PageRequest.of(0, 10);
		Notification mockNotification = makeMockNotification(mockReceivcer, mockSender, "/post/1", 1L);
		Slice<Notification> mockSliceNotification = new SliceImpl<>(
				List.of(mockNotification), pageRequest, true
		);
		given(notificationRepository.findNotificationByReceiver(mockReceivcer, 2L, pageRequest))
				.willReturn(mockSliceNotification);

		// when
		NotificationResDto response = notificationService
				.findNotifications(receiverId, 2L, 10);

		// then
		assertThat(response).isNotNull();
		assertThat(response.hasNext()).isEqualTo(true);
		assertThat(response.lastNotificationId()).isEqualTo(1L);
		assertThat(response.content().get(0).redirectUrl()).isEqualTo("/post/1");
		then(userRepository).should().getReferenceById(receiverId);
		then(notificationRepository).should().findNotificationByReceiver(mockReceivcer, 2L, pageRequest);
	}

	@Test
	@DisplayName("알림 조회 - 성공")
	void readNotification_success() {
		// given
		final Long receiverId = 1L;
		User mockReceivcer = makeMockUser("sdfdsz", receiverId);
		given(userRepository.getReferenceById(mockReceivcer.getId())).willReturn(mockReceivcer);

		final Long notificationId = 1L;
		User mockSender = makeMockUser("dkxzzz", 2L);
		Notification mockNotification = makeMockNotification(mockReceivcer, mockSender, "/post/1", notificationId);
		given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.of(mockNotification));

		// when
		notificationService.readNotification(receiverId, notificationId);

		// then
		assertThat(mockNotification.isRead()).isTrue();
		then(userRepository).should().getReferenceById(receiverId);
		then(notificationRepository).should().findByIdAndReceiver(notificationId, mockReceivcer);
	}

	@Test
	@DisplayName("알림 조회 - 실패: 알림을 찾을 수 없습니다")
	void readNotification_fail_notificationNotFound() {
		// given
		final Long receiverId = 1L;
		User mockReceivcer = makeMockUser("sdfdsz", receiverId);
		given(userRepository.getReferenceById(mockReceivcer.getId())).willReturn(mockReceivcer);

		final Long notificationId = 1L;
		given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> notificationService.readNotification(receiverId, notificationId))
				.isInstanceOf(NotificationNotFoundException.class);
	}

	@Test
	@DisplayName("알림 삭제 - 성공")
	void deleteNotification_success() {
		// given
		final Long receiverId = 1L;
		User mockReceivcer = makeMockUser("sdfdsz", receiverId);
		given(userRepository.getReferenceById(mockReceivcer.getId())).willReturn(mockReceivcer);

		final Long notificationId = 1L;
		User mockSender = makeMockUser("dkxzzz", 2L);
		Notification mockNotification = makeMockNotification(mockReceivcer, mockSender, "/post/1", notificationId);
		given(notificationRepository.findByIdAndReceiver(notificationId, mockReceivcer)).willReturn(Optional.of(mockNotification));

		// when
		notificationService.readNotification(receiverId, notificationId);

		// then
		assertThat(mockNotification.isRead()).isTrue();
		then(userRepository).should().getReferenceById(receiverId);
		then(notificationRepository).should().findByIdAndReceiver(notificationId, mockReceivcer);
	}

}
