package dev.kyudong.back.user.api.dto.event;

public record UserImageDeleteEvent(
		Long userId,
		Long prevFileId
) {
	public static UserImageDeleteEvent of(Long userId, Long prevFileId) {
		return new UserImageDeleteEvent(userId, prevFileId);
	}
}
