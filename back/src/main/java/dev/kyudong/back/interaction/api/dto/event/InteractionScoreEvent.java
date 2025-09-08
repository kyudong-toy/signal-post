package dev.kyudong.back.interaction.api.dto.event;

/**
 * 피드 목록 점수 계산에 사용됩니다
 * @param targetId		피드(게시글) 아이디
 * @param scoreDelta	점수
 */
public record InteractionScoreEvent(
		Long targetId,
		double scoreDelta
) {
}
