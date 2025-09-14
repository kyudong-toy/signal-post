package dev.kyudong.back.notification.api.dto.res;

import dev.kyudong.back.notification.api.dto.NotificationQueryDto;
import dev.kyudong.back.notification.domain.NotificationType;

/**
 * 알림 메시지 생성을 위한 재료 데이터 (메타데이터)
 */
public record NotificationContextDto(
		Long postId,
		String postSubject,		// 제목이 있을 경우
		String postSummary,		// 제목이 없다면 사용

		Long commentId,
		String commentContent
) {
	public static NotificationContextDto from(NotificationQueryDto queryDto) {
		Long postId = null;
		String postSubject = null;
		String postSummary = null;

		Long commentId = null;
		String commentContent = null;

		if (queryDto.type().equals(NotificationType.POST)) {
			postId = queryDto.postId();
			postSubject = queryDto.postSubject();
			postSummary = queryDto.postSummary();
		} else if (queryDto.type().equals(NotificationType.COMMENT)) {
			commentId = queryDto.commentId();
			commentContent = queryDto.commentContent();
		}

		return new NotificationContextDto(
				postId, postSubject, postSummary, commentId, commentContent
		);
	}
}
