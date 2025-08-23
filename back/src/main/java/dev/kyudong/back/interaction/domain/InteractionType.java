package dev.kyudong.back.interaction.domain;

public enum InteractionType {

	// 긍정적
	LIKE,		// 좋아요
	LOVE,		// 사랑해요
	LAUGH,		// 재밌어요
	COOL,		// 멋져요
	SAD,		// 슬퍼요

	// 부정적
	DISLIKE,	// 싫어요
	ANGRY;		// 화나요

	public boolean isPostive() {
		return this == LIKE || this == LOVE || this ==  LAUGH
				|| this == COOL || this == SAD;
	}

	public boolean isNegative() {
		return this == DISLIKE || this == ANGRY;
	}

}
