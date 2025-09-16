package dev.kyudong.back.testhelper.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatMessage;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.MessageType;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.notification.domain.NotificationType;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 유닛 테스트시 상속받아 사용해주세요
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public abstract class UnitTestBase {

	/* 사용자(User) 관련 공용 테스트 도구 */
	protected User defaultUserSetting(User mockUser) {
		mockUser.updateDisplayName("mockDisplayName");
		mockUser.updateBio("Hello I`m Mock");
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		ReflectionTestUtils.setField(mockUser, "createdAt", Instant.now());
		return mockUser;
	}

	protected User createMockUser() {
		User mockUser = User.create(
				"mockUser",
				"rawPassord",
				"encodedPassword"
		);
		return defaultUserSetting(mockUser);
	}

	protected User createMockUser(String username, Long userId) {
		User mockUser = User.create(
				username,
				"rawPassord",
				"encodedPassword"
		);
		mockUser.updateDisplayName("mockDisplayName");
		mockUser.updateBio("Hello I`m Mock");
		ReflectionTestUtils.setField(mockUser, "id", userId);
		ReflectionTestUtils.setField(mockUser, "createdAt", Instant.now());
		return mockUser;
	}

	/* 게시글(Post) 관련 공용 테스트 도구 */
	protected Post createMockPost(User mockUser) throws JsonProcessingException {
		Post mockPost = Post.create("제목", createMockContent());
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		ReflectionTestUtils.setField(mockPost, "viewCount", 0L);
		ReflectionTestUtils.setField(mockPost, "score", 0D);
		ReflectionTestUtils.setField(mockPost, "createdAt", Instant.now());
		ReflectionTestUtils.setField(mockPost, "modifiedAt", Instant.now());
		return mockPost;
	}

	protected Comment makeMockComment(Post mockPost, User mockUser) throws JsonProcessingException {
		Comment mockComment = Comment.create(createMockContent(), mockUser);
		ReflectionTestUtils.setField(mockComment, "id", 1L);
		ReflectionTestUtils.setField(mockComment, "post", mockPost);
		ReflectionTestUtils.setField(mockComment, "createdAt", Instant.now());
		ReflectionTestUtils.setField(mockComment, "modifiedAt", Instant.now());
		return mockComment;
	}

	/* 알림(Notification) 관련 공용 테스트 도구 */
	protected Notification createMockNotification(User receiver, User sender) {
		Notification mockNotification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl("/post/1")
				.type(NotificationType.POST)
				.build();
		ReflectionTestUtils.setField(mockNotification, "id", 1L);
		return mockNotification;
	}

	protected Notification createMockNotification(User receiver, User sender, Long notificationId) {
		Notification mockNotification = Notification.builder()
				.receiver(receiver)
				.sender(sender)
				.redirectUrl("/post/1")
				.type(NotificationType.POST)
				.build();
		ReflectionTestUtils.setField(mockNotification, "id", notificationId);
		return mockNotification;
	}

	/* 팔로우(Follow) 관련 공용 테스트 도구 */
	protected Follow createMockFollow(User follower, User following, Long id) {
		Follow follow = Follow.create(follower, following);
		ReflectionTestUtils.setField(follow, "id", id);
		return follow;
	}

	/* 채팅(Chat) 관련 공용 테스트 도구 */
	protected ChatRoom createMockChatRoom(List<User> userList) {
		ChatRoom mockChatRoom = ChatRoom.builder()
				.initUserList(userList)
				.build();
		ReflectionTestUtils.setField(mockChatRoom, "id", 1L);
		ReflectionTestUtils.setField(mockChatRoom, "createdAt", Instant.now());
		return mockChatRoom;
	}

	protected ChatMember createMockChatMember(User user) {
		ChatMember mockChatMember = ChatMember.builder()
				.user(user)
				.build();
		ReflectionTestUtils.setField(mockChatMember, "id", 1L);
		return mockChatMember;
	}

	protected ChatMessage makeMockChatMessage(ChatMember sender, Long id) {
		ChatMessage newChatMessage = ChatMessage.builder()
				.sender(sender)
				.content("본문")
				.messageType(MessageType.TEXT)
				.build();
		ReflectionTestUtils.setField(newChatMessage, "id", id);
		ReflectionTestUtils.setField(newChatMessage, "createdAt", Instant.now());
		return newChatMessage;
	}

	/* 글작성 콘텐츠(Common) 관련 공용 테스트 도구 */
	protected String createMockContent() throws JsonProcessingException {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"contents", List.of(textNode)
		);

		Map<String, Object> map = Map.of(
				"type", "doc",
				"contents", List.of(paragraphNode)
		);

		return new ObjectMapper().writeValueAsString(map);
	}

	protected Map<String, Object> createMockContentObject() {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"contents", List.of(textNode)
		);

		return Map.of(
				"type", "doc",
				"contents", List.of(paragraphNode)
		);
	}

}
