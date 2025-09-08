package dev.kyudong.back.post.application.port.in.event;

import dev.kyudong.back.interaction.api.dto.event.InteractionScoreEvent;

public interface PostEventUsecase {

	/**
	 * 상호작용 후 해당 게시글({@link dev.kyudong.back.post.domain.entity.Post})에 점수를 수정합니다.
	 * @param event 게시글 아이디와 점수
	 */
	void handlePostScoreUpdate(InteractionScoreEvent event);

}
