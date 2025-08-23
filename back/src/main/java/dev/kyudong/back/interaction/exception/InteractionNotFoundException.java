package dev.kyudong.back.interaction.exception;

public class InteractionNotFoundException extends RuntimeException {
	public InteractionNotFoundException(Long userId, Long targetId) {
		super(String.format("상호작용한 기록이 없습니다: userId=%d, targetId=%d", userId, targetId));
	}
}
