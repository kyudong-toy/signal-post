package dev.kyudong.back.user.domain;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.domain.Comment;
import dev.kyudong.back.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@Table(name = "USERS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@Column(name = "USER_NAME", unique = true, nullable = false, length = 30)
	private String userName;

	@Column(name = "PASS_WORD", nullable = false, length = 150)
	private String passWord;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false)
	private UserStatus status;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private final List<Post> postList = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private final List<Comment> commentList = new ArrayList<>();

	@Builder
	public User(String userName, String passWord) {
		this.userName = userName;
		this.passWord = passWord;
		this.status = UserStatus.ACTIVE;
		this.createdAt = Instant.now();
	}

	public void updatepassWord(String newpassWord) {
		if (!StringUtils.hasText(newpassWord)) {
			throw new InvalidInputException("Password must not be null");
		}
		if (newpassWord.length() < 4) {
			throw new InvalidInputException("Password must be at least 4 characters long.");
		}
		if (newpassWord.length() > 150) {
			throw new InvalidInputException("Password cannot be longer than 150 characters.");
		}
		this.passWord = newpassWord;
	}

	public void activeUser() {
		this.status = UserStatus.ACTIVE;
	}

	public void dormantUser() {
		this.status = UserStatus.DORMANT;
	}

	public void deleteUser() {
		this.status = UserStatus.DELETED;
	}

	public void addPost(@NonNull Post post) {
		this.postList.add(post);
		post.associateUser(this);
	}

}
