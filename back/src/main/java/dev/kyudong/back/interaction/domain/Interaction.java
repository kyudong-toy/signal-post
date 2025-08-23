package dev.kyudong.back.interaction.domain;

import dev.kyudong.back.interaction.exception.InteractionTypeException;
import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Arrays;

@Entity
@Getter
@Table(name = "INTERACTIONS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", nullable = false)
	private User user;

	@Column(name = "TARGET_ID", nullable = false)
	private Long targetId;

	@Enumerated(EnumType.STRING)
	@Column(name = "TARGET_TYPE", nullable = false, length = 20)
	private TargetType targetType;

	@Enumerated(EnumType.STRING)
	@Column(name = "INTERACTION_TYPE", length = 20, nullable = false)
	private InteractionType interactionType;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(name = "MODIFIED_AT")
	private Instant modifiedAt;

	@Builder
	private Interaction(User user, Long targetId, TargetType targetType, InteractionType interactionType) {
		this.user = user;
		this.targetId = targetId;
		this.targetType = targetType;
		this.interactionType = interactionType;
	}

	public void updateInteractionType(InteractionType interactionType) {
		this.interactionType = Arrays.stream(InteractionType.values())
				.filter(interactionType::equals)
				.findFirst()
				.orElseThrow(() -> new InteractionTypeException(interactionType));
	}

}
