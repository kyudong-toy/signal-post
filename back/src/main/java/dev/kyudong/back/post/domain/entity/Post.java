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
import java.util.*;

@Entity
@Getter
@ToString(exclude = {"user"})
@Table(name = "POSTS")
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

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "CONTENT", nullable = false, columnDefinition = "jsonb")
	private String content;

	@Column(name = "POST_VIEW_COUNT")
	private Long postViewCount;

	@Column(name = "POST_SCORE")
	private Double postScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORY_ID", nullable = false)
	private Category category;

	@ManyToMany(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.PERSIST, CascadeType.MERGE}
	)
	@JoinTable(name = "POST_TAG",
			joinColumns = @JoinColumn(name = "POST_ID"),
			inverseJoinColumns = @JoinColumn(name = "TAG_ID")
	)
	private Set<Tag> tags = new HashSet<>();

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

	private Post(String subject, String content, Category category) {
		validSubject(subject);
		validContent(content);
		this.subject = subject;
		this.content = content;
		this.category = category;
		this.status = PostStatus.NORMAL;
	}

	public static Post of(String subject, String content, Category category) {
		return new Post(subject, content, category);
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

	public void increaseCount() {
		this.postViewCount++;
	}

	public void updateScore(double score) {
		this.postScore += score;
	}

	/**
	 * addPost를 이용한 호출을 권장합니다.
	 * @param user 게시글 소유자.
	 */
	public void associateUser(User user) {
		this.user = user;
	}

	public void associateCategory(Category category) {
		this.category = category;
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

	public void addTags(@NonNull Set<Tag> tags) {
		this.tags.addAll(tags);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Post post = (Post) o;

		return Objects.equals(id, post.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, user);
	}

}
