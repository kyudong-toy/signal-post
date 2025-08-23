package dev.kyudong.back.interaction.service;

import dev.kyudong.back.interaction.api.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.exception.InteractionNotFoundException;
import dev.kyudong.back.interaction.repository.InteractionRepository;
import dev.kyudong.back.post.exception.CommentNotFoundException;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.repository.CommentRepository;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionService {

	private final InteractionRepository interactionRepository;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;

	@Transactional
	public InteractionResDto doInteraction(final Long userId, TargetType targetType, final Long targetId, InteractionReqDto request) {
		log.debug("상호작용을 시작합니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);

		validateTargetType(targetType, targetId);

		User user = userRepository.getReferenceById(userId);
		Interaction interaction;
		if (interactionRepository.existsByUserAndTargetId(user, targetId)) {
			interaction = interactionRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType)
					.orElseThrow(() -> {
						log.warn("상호작용한 기록이 없습니다: userId={}, targetId={}, targetType={}", user.getId(), targetId, targetType);
						return new InteractionNotFoundException(user.getId(), targetId);
					});
			interaction.updateInteractionType(request.interactionType());
		} else {
			interaction = Interaction.builder()
					.user(user)
					.targetType(targetType)
					.targetId(targetId)
					.interactionType(request.interactionType())
					.build();
			interactionRepository.save(interaction);
		}

		log.info("상호작용이 완료되었습니다: userId={}, targetType={}, targetId={}, interactionType={}", userId, targetType.name(), targetId, request.interactionType());
		return InteractionResDto.from(interaction);
	}

	@Transactional
	public void deleteInteraction(final Long userId, TargetType targetType, final Long targetId) {
		log.debug("상호작용 취소를 시작합니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);

		validateTargetType(targetType, targetId);

		User user = userRepository.getReferenceById(userId);
		Interaction interaction = interactionRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType)
				.orElseThrow(() -> {
					log.warn("상호작용한 기록이 없습니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);
					return new InteractionNotFoundException(user.getId(), targetId);
				});

		interactionRepository.delete(interaction);

		log.info("상호작용 삭제가 완료되었습니다: userId={}, targetType={}, targetId={}", userId, targetType.name(), targetId);
	}

	private void validateTargetType(TargetType targetType, final Long targetId) {
		if (Objects.requireNonNull(targetType) == TargetType.POST) {
			if (!postRepository.existsById(targetId)) {
				throw new PostNotFoundException(targetId);
			}
		} else if (Objects.requireNonNull(targetType) == TargetType.COMMENT) {
			if (!commentRepository.existsById(targetId)) {
				throw new CommentNotFoundException(targetId);
			}
		} else {
			throw new IllegalArgumentException(String.format("지원하지 않는 targetType 입니다: targetType=%s", targetType));
		}
	}

}
