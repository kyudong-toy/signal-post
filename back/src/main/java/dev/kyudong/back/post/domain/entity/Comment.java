package dev.kyudong.back.post.domain.entity;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Entity
@Getter
@ToString(exclude = {"post", "user"}) // post, user 무한 참조 방지
@Table(name = "COMMENTS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POST_ID", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "CONTENT", nullable = false, columnDefinition = "jsonb")
	private String content;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private CommentStatus status;

	private Comment(String content, @NonNull User user) {
		validContent(content);
		this.content = content;
		this.user = user;
		this.status = CommentStatus.NORMAL;
	}

	public static Comment create(String content, User user) {
		return new Comment(content, user);
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
		this.status = CommentStatus.DELETED;
	}

	public void restore() {
		this.status = CommentStatus.NORMAL;
	}

	/**
	 * addComment를 이용한 호출을 권장합니다.
	 * @param post 댓글이 작성된 게시글.
	 */
	public void associatePost(@NonNull Post post) {
		this.post = post;
	}

}
