package dev.kyudong.back.interaction.domain;

import java.util.Arrays;
import java.util.List;

public enum InteractionType {

	// 긍정적
	LIKE(1.0d),		// 좋아요
	LOVE(1.3d),		// 사랑해요
	LAUGH(1.2d),		// 재밌어요
	COOL(1.1d),		// 멋져요
	MOVED(1.1d),		// 감동이에요
	WOW(1.15d),		// 놀라워요
	CUTE(1.15d),		// 귀여움!

	// 부정적
	DISLIKE(-0.15d),	// 싫어요
	SHOCKED(-0.25d),	// 충격이에요
	BORED(-0.1d),		// 관심없어요
	DISGUST(-0.5d),	// 역겨워!
	SCARY(-0.35d),		// 무서워요
	ANGRY(-0.3d);		// 화나요

	final double interactionScore;

	InteractionType(double interactionScore) {
		this.interactionScore = interactionScore;
	}

	public double getInteractionScore() {
		return interactionScore;
	}

	public boolean isPostive() {
		return this == LIKE || this == LOVE || this ==  LAUGH
				|| this == COOL || this == MOVED || this == WOW || this == CUTE;
	}

	public static List<InteractionType> getPositiveTypes() {
		return Arrays.stream(values())
				.filter(InteractionType::isPostive)
				.toList();
	}

	public boolean isNegative() {
		return this == DISLIKE || this == SHOCKED || this == BORED
				|| this == DISGUST || this == SCARY || this == ANGRY ;
	}

	public static List<InteractionType> getNegativeTypes() {
		return Arrays.stream(values())
				.filter(InteractionType::isNegative)
				.toList();
	}

}
