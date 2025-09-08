package dev.kyudong.back.interaction.strategy;

import dev.kyudong.back.interaction.domain.TargetType;

public interface InteractionStrategy {

	boolean supports(TargetType targetType);

	void existsTarget(Long targetId);

}
