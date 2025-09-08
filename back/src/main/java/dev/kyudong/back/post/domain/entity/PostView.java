package dev.kyudong.back.post.domain.entity;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@ToString(exclude = {"user", "post"})
@Table(
		name = "POST_VIEW",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_post_view_user",
						columnNames = {"USER_ID", "POST_ID"}
				)
		}
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostView {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POST_ID")
	private Post post;

	@CreatedDate
	@Column(name = "FIRST_VIEW_AT", updatable = false)
	private Instant firstViewAt;

	@LastModifiedDate
	@Column(name = "LAST_VIEW_AT")
	private Instant lastViewAt;

	private PostView(User user, Post post) {
		this.user = user;
		this.post = post;
	}

	public static PostView of(User user, Post post) {
		return new PostView(user, post);
	}

}
