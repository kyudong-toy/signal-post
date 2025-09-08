package dev.kyudong.back.interaction.api.dto.res;

import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.InteractionType;
import dev.kyudong.back.interaction.domain.TargetType;

public record InteractionResDto(
		Long id,
		Long userId,
		TargetType targetType,
		Long targetId,
		InteractionType interactionType
) {
	public static InteractionResDto from(Interaction interaction) {
		return new InteractionResDto(
			interaction.getId(),
			interaction.getUser().getId(),
			interaction.getTargetType(),
			interaction.getTargetId(),
			interaction.getInteractionType()
		);
	}
}
