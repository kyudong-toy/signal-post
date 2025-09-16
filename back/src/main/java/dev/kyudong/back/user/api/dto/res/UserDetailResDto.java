package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;

public record UserDetailResDto(
		Long userId,
		String username,
		String displayName,
		String bio,
		String profileImageUrl,
		String backGroundImageUrl,
		boolean isOwner
) {
	public static UserDetailResDto of(User user, boolean isOwner) {
		return new UserDetailResDto(
				user.getId(),
				user.getUsername(),
				user.getDisplayName(),
				user.getBio(),
				user.getProfileImageUrl(),
				user.getBackgroundImageUrl(),
				isOwner
		);
	}
}
