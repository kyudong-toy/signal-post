package dev.kyudong.back.interaction.exception;

import dev.kyudong.back.interaction.domain.TargetType;

public class InteractionTargetNotFoundException extends RuntimeException {
	public InteractionTargetNotFoundException(TargetType targetType) {
		super("해당 타입은 상호작용을 지원하지 않습니다: targetType=" + targetType.name());
	}
}
