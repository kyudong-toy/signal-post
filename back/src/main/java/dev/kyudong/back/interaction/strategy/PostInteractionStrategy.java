package dev.kyudong.back.interaction.strategy;

import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostInteractionStrategy implements InteractionStrategy {

	private final PostUsecase postUsecase;

	@Override
	public boolean supports(TargetType targetType) {
		return targetType == TargetType.POST;
	}

	@Override
	public void existsTarget(Long targetId) {
		postUsecase.validatePostExists(targetId);
	}

}
