package dev.kyudong.back.user.api.dto.res;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.domain.UserStatus;

public record UserProfileUpdateResDto(
		Long id,
		String username,
		String displayName,
		String bio,
		String profileImageUrl,
		String backGroundImageUrl,
		UserStatus status
) {
	public static UserProfileUpdateResDto from(User user) {
		return new UserProfileUpdateResDto(
				user.getId(),
				user.getUsername(),
				user.getDisplayName(),
				user.getBio(),
				user.getProfileImageUrl(),
				user.getBackgroundImageUrl(),
				user.getStatus()
		);
	}
}
