package dev.kyudong.back.interaction.exception;

import dev.kyudong.back.interaction.domain.InteractionType;

public class InteractionTypeException extends RuntimeException {
	public InteractionTypeException(InteractionType interactionType) {
		super("현재 지원되지 않는 유형입니다: interactionType=" + interactionType);
	}
}
