package dev.kyudong.back.chat.domain;

import dev.kyudong.back.file.domain.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Table(name = "CHAT_MESSAGES")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SENDER_ID", nullable = false)
	private ChatMember sender;

	@Column(length = 1000, nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "MESSAGE_TYPE", nullable = false, length = 20)
	private MessageType messageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "MESSAGE_STATUS", nullable = false, length = 20)
	private MessageStatus messageStatus;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FILE_ID")
	private File file;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@Column(name = "DELETE_AT", updatable = false)
	private Instant deleteAt;

	@Builder
	private ChatMessage(ChatMember sender, String content, MessageType messageType, File file) {
		this.sender = sender;
		this.content = content;
		this.messageType = messageType;
		this.messageStatus = MessageStatus.ACTIVE;
		this.file = file;
	}

	public void associateChatRoom(ChatRoom chatRoom) {
		this.chatRoom = chatRoom;
	}

	public void delete() {
		this.messageStatus = MessageStatus.DELETED;
		this.deleteAt = Instant.now();
	}

}
