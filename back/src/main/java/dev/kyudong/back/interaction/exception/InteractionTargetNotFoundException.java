package dev.kyudong.back.interaction.exception;

public class InteractionTargetNotFoundException extends RuntimeException {
	public InteractionTargetNotFoundException(Long targetId) {
		super(targetId + "를 찾을 수 없습니다");
	}
}
