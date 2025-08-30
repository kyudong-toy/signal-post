package dev.kyudong.back.chat.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@ToString(exclude = {"chatRoom"})
@Table(
		name = "CHAT_MEMBERS",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_chat_user_chatroom",
						columnNames = {"CHAT_ROOM_ID", "USER_ID"}
				)
		}
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@Column(name = "ROOM_NAME", nullable = false, length = 150)
	private String roomName;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private MemberStatus status;

	@CreatedDate
	@Column(name = "JOINED_AT", updatable = false)
	private Instant joinedt;

	@Builder
	private ChatMember(ChatRoom chatRoom, User user, String roomName) {
		this.chatRoom = chatRoom;
		this.user = user;
		this.roomName = roomName;
		this.status = MemberStatus.JOINED;
	}

	public void leave() {
		this.status = MemberStatus.LEFT;
	}

	public void block() {
		this.status = MemberStatus.BLOCKED;
	}

	public void kick() {
		this.status = MemberStatus.KICK;
	}

	public void updateRoomName(String roomName) {
		this.roomName = roomName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChatMember that = (ChatMember) o;

		if (!Objects.equals(id, that.id)) return false;
		return Objects.equals(user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chatRoom, user);
	}

}
