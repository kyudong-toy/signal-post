package dev.kyudong.back.chat.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Table(name = "CHAT_ROOMS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@OneToMany(
			fetch = FetchType.LAZY,
			cascade = CascadeType.PERSIST,
			mappedBy = "chatRoom"
	)
	private final Set<ChatMember> chatMembers = new HashSet<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "chatRoom")
	@BatchSize(size = 50)
	private List<ChatMessage> chatMessages = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private RoomStatus status;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Column(name = "LAST_ACTIVITY_AT")
	private Instant lastActivityAt;

	@Builder
	private ChatRoom(List<User> initUserList) {
		this.addNewMember(initUserList);
		this.status = RoomStatus.ACTIVE;
	}

	public Set<ChatMember> addNewMember(List<User> newUserList) {
		Set<ChatMember> newMembers = new HashSet<>();

		newUserList.forEach(currentUser -> {
			List<User> otherUsers = newUserList.stream()
					.filter(user -> !user.equals(currentUser))
					.toList();

			String roomName;
			if (otherUsers.isEmpty()) {
				roomName = "나와의 대화";
			} else if (otherUsers.size() == 1) {
				roomName = otherUsers.get(0).getUsername();
			} else {
				roomName = otherUsers.stream()
						.map(User::getUsername)
						.limit(4)
						.collect(Collectors.joining(", "))
						.trim();
			}

			ChatMember chatMember = ChatMember.builder()
					.chatRoom(this)
					.user(currentUser)
					.roomName(roomName)
					.build();
			this.chatMembers.add(chatMember);
			newMembers.add(chatMember);
		});

		updateLastActivityAt();
		return newMembers;
	}

	public void addMessage(@NonNull ChatMessage chatMessage) {
		this.chatMessages.add(chatMessage);
		chatMessage.associateChatRoom(this);
		updateLastActivityAt();
	}

	/**
	 * 채팅방의 활동일을 갱신합니다
	 */
	private void updateLastActivityAt() {
		this.lastActivityAt = Instant.now();
	}

}
