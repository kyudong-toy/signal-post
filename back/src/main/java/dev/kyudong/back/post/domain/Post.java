package dev.kyudong.back.post.domain;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@Table(name = "POST")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@Column(name = "SUBJECT", length = 100, nullable = false)
	private String subject;

	@Lob
	@Column(name = "CONTENT", nullable = false)
	private String content;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private PostStatus status;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
	private List<Comment> commentList = new ArrayList<>();

	@Builder
	public Post(String subject, String content) {
		validSubject(subject);
		validContent(content);
		Instant now = Instant.now();
		this.subject = subject;
		this.content = content;
		this.createdAt = now;
		this.modifiedAt = now;
		this.status = PostStatus.NORMAL;
	}

	public void updateSubject(String subject) {
		validSubject(subject);
		this.subject = subject;
	}

	private void validSubject(String subject) {
		if (!StringUtils.hasText(subject)) {
			throw new InvalidInputException("Subject must not be null");
		}
		if (subject.length() > 100) {
			throw new InvalidInputException("Subject cannot be longer than 100 characters.");
		}
	}

	public void updateContent(String content) {
		validContent(content);
		this.content = content;
	}

	private void validContent(String content) {
		if (!StringUtils.hasText(content)) {
			throw new InvalidInputException("Content must not be null");
		}
	}

	public void delete() {
		this.status = PostStatus.DELETED;
	}

	public void restore() {
		this.status = PostStatus.NORMAL;
	}

	/**
	 * addPost를 이용한 호출을 권장합니다.
	 * @param user 게시글 소유자.
	 */
	public void associateUser(User user) {
		this.user = user;
	}

	public void addComment(@NonNull Comment comment) {
		this.commentList.add(comment);
		comment.associatePost(this);
	}

	public void addComments(@NonNull List<Comment> comments) {
		this.commentList.addAll(comments);
		for (Comment comment : comments) {
			comment.associatePost(this);
		}
	}

}
