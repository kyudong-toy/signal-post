package dev.kyudong.back.user.domain;

import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "USERS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@Column(name = "USER_NAME", unique = true, nullable = false, length = 30)
	private String username;

	@Column(name = "PASS_WORD", nullable = false, length = 150)
	private String password;

	@Column(name = "DISPLAY_NAME", nullable = false, length = 20)
	private String displayName;

	@Column(name = "USER_BIO", length = 150)
	private String bio;

	@Column(name = "PROFILE_IMAGE_URL")
	private String profileImageUrl;

	@Column(name = "BACKGROUND_IMAGE_URL")
	private String backgroundImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private UserStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", nullable = false, length = 20)
	private UserRole role;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private List<Post> postList = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private List<Comment> commentList = new ArrayList<>();

	@Builder
	private User(String username, String rawPassword, String encodedPassword, String displayName, UserRole role) {
		validateUsername(username);
		this.username = username;

		validatePassword(rawPassword);
		this.password = encodedPassword;

		validateDisplayName(displayName);
		this.displayName = displayName;

		this.status = UserStatus.ACTIVE;
		this.role = role;
	}

	public static User create(String username, String rawPassword, String encodedPassword, String displayName) {
		return new User(username, rawPassword, encodedPassword, displayName, UserRole.USER);
	}

	public static User create(String username, String rawPassword, String encodedPassword, String displayName, UserRole role) {
		return new User(username, rawPassword, encodedPassword, displayName, role);
	}

	private void validateUsername(String username) {
		if (!StringUtils.hasText(username)) {
			throw new InvalidInputException("Username must not be null");
		}
		if (username.length() < 4) {
			throw new InvalidInputException("Username must be at least 4 characters long.");
		}
		if (username.length() > 30) {
			throw new InvalidInputException("Username cannot be longer than 30 characters.");
		}
	}

	public void updatepassWord(String rawPassword, String encodedPassword) {
		validatePassword(rawPassword);
		this.password = encodedPassword;
	}

	private void validatePassword(String rawPassword) {
		if (!StringUtils.hasText(rawPassword)) {
			throw new InvalidInputException("Password must not be null");
		}
		if (rawPassword.length() < 4) {
			throw new InvalidInputException("Password must be at least 4 characters long.");
		}
		if (rawPassword.length() > 150) {
			throw new InvalidInputException("Password cannot be longer than 150 characters.");
		}
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

	public void updateDisplayName(String displayName) {
		validateDisplayName(displayName);
		this.displayName = displayName;
	}

	private void validateDisplayName(String displayName) {
		if (!StringUtils.hasText(displayName)) {
			throw new InvalidInputException("DisplayName must not be null");
		}
		if (username.length() < 3) {
			throw new InvalidInputException("DisplayName must be at least 3 characters long.");
		}
		if (username.length() > 20) {
			throw new InvalidInputException("DisplayName cannot be longer than 20 characters.");
		}
	}

	public void updateBio(String bio) {
		this.bio = bio;
	}

	public void updateProfileImage(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public void updateBackGroundImage(String backGroundImageUrl) {
		this.backgroundImageUrl = backGroundImageUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		return Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
