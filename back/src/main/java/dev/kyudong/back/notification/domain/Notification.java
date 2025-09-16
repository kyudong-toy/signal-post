package dev.kyudong.back.notification.domain;

import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@ToString(exclude = {"receiver", "sender"})
@Table(name = "NOTIFICATIONS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RECEIVER_ID", nullable = false)
	private User receiver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SENDER_ID", nullable = false)
	private User sender;

	@Column(name = "REDIRECT_URL")
	private String redirectUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POST_ID")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMMENT_Id")
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", nullable = false)
	private NotificationType type;

	@Column(name = "IS_READ", nullable = false)
	private boolean isRead;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "READ_AT", updatable = false)
	private Instant readAt;

	@Builder
	private Notification(User receiver, User sender, String redirectUrl, Post post, Comment comment, NotificationType type) {
		this.receiver = receiver;
		this.sender = sender;
		this.redirectUrl = redirectUrl;
		this.type = type;
		this.post = post;
		this.comment = comment;
		this.isRead = false;
	}

	public void read() {
		this.isRead = true;
	}

}
