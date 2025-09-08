package dev.kyudong.back.post.application.service.event;

import dev.kyudong.back.interaction.api.dto.event.InteractionScoreEvent;
import dev.kyudong.back.post.application.port.in.event.PostEventUsecase;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventService implements PostEventUsecase {

	private final PostUsecase postUsecase;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePostScoreUpdate(InteractionScoreEvent event) {
		log.debug("게시글 점수 이벤트 수신완료: targetId={}", event.targetId());

		Post post = postUsecase.getPostEntityOrThrow(event.targetId());
		post.updateScore(event.scoreDelta());
	}

}
