package dev.kyudong.back.user.api.dto.event;

public record UserImagePublishEvent(
		Long userId,
		Long fileId
) {
	public static UserImagePublishEvent of(Long userId, Long fileId) {
		return new UserImagePublishEvent(userId, fileId);
	}
}
