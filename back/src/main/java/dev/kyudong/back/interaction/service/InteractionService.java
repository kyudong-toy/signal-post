package dev.kyudong.back.interaction.service;

import dev.kyudong.back.interaction.api.dto.event.InteractionScoreEvent;
import dev.kyudong.back.interaction.api.dto.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.dto.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.exception.InteractionNotFoundException;
import dev.kyudong.back.interaction.exception.InteractionTargetNotFoundException;
import dev.kyudong.back.interaction.repository.InteractionRepository;
import dev.kyudong.back.interaction.strategy.InteractionStrategy;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionService {

	private final InteractionRepository interactionRepository;
	private final UserReaderService userReaderService;
	private final List<InteractionStrategy> strategies;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Transactional
	public InteractionResDto doInteraction(final Long userId, TargetType targetType, final Long targetId, InteractionReqDto request) {
		log.debug("상호작용을 시작합니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);

		InteractionStrategy interactionStrategy = findInteractionStrategy(targetType);
		interactionStrategy.existsTarget(targetId);

		double score;
		Interaction interaction;
		User user = userReaderService.getUserReference(userId);
		Optional<Interaction> existingInteractionOpt = interactionRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType);
		if (existingInteractionOpt.isPresent()) {
			interaction = existingInteractionOpt.get();

			double oldScore = interaction.getInteractionType().getInteractionScore();
			double newScore = request.interactionType().getInteractionScore();

			score = newScore - oldScore;
			interaction.updateInteractionType(request.interactionType());
		} else {
			score = request.interactionType().getInteractionScore();
			interaction = Interaction.builder()
					.user(user)
					.targetType(targetType)
					.targetId(targetId)
					.interactionType(request.interactionType())
					.build();
			interactionRepository.save(interaction);
		}

		// 점수계산
		InteractionScoreEvent scoreEvent = new InteractionScoreEvent(targetId, score);
		applicationEventPublisher.publishEvent(scoreEvent);

		log.debug("상호작용이 완료되었습니다: userId={}, targetType={}, targetId={}, interactionType={}", userId, targetType.name(), targetId, request.interactionType());
		return InteractionResDto.from(interaction);
	}

	@Transactional
	public void deleteInteraction(final Long userId, TargetType targetType, final Long targetId) {
		log.debug("상호작용 취소를 시작합니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);

		InteractionStrategy interactionStrategy = findInteractionStrategy(targetType);
		interactionStrategy.existsTarget(targetId);

		User user = userReaderService.getUserReference(userId);
		Interaction interaction = interactionRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType)
				.orElseThrow(() -> {
					log.warn("상호작용한 기록이 없습니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);
					return new InteractionNotFoundException(user.getId(), targetId);
				});

		interactionRepository.delete(interaction);

		log.debug("상호작용 삭제가 완료되었습니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);
	}

	private InteractionStrategy findInteractionStrategy(TargetType targetType) {
		return strategies.stream()
				.filter(s -> s.supports(targetType))
				.findFirst()
				.orElseThrow(() -> new InteractionTargetNotFoundException(targetType));
	}

}
