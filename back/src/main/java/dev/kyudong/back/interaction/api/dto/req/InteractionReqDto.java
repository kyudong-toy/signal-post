package dev.kyudong.back.interaction.api.dto.req;

import dev.kyudong.back.interaction.domain.InteractionType;
import jakarta.validation.constraints.NotNull;

public record InteractionReqDto(
		@NotNull(message = "상호작용 유형은 필수입니다")
		InteractionType interactionType
) {
}
