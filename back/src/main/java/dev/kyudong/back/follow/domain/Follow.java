package dev.kyudong.back.follow.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@ToString(exclude = {"follower", "following"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "FOLLOWS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FOLLOWER_ID", nullable = false)
	private User follower;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FOLLOWING_ID", nullable = false)
	private User following;

	@Enumerated(EnumType.STRING)
	@Column(name = "FOLLOW_STATUS", nullable = false, length = 20)
	private FollowStatus status;

	@CreatedDate
	@Column(name = "FOLLOWED_AT", updatable = false)
	private Instant followedAt;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Builder
	private Follow(User follower, User following) {
		this.follower = follower;
		this.following = following;
		this.status = FollowStatus.PENDING;
	}

	public void accept() {
		this.status = FollowStatus.FOLLOWING;
	}

	public void block() {
		this.status = FollowStatus.BLOCKED;
	}

	public void unfollow() {
		this.status = FollowStatus.UNFOLLOWED;
	}

}
