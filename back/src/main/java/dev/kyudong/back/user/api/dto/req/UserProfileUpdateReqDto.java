package dev.kyudong.back.user.api.dto.req;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateReqDto(
		@Size(max = 30, message = "최대 30자까지 입력가능합니다")
		String displayName,

		@Size(max = 150, message = "자기소개는 최대 150자까지 입력가능합니다")
		String bio,

		String profileImageUrl,

		@Positive(message = "잘못된 아이디입니다")
		Long profileImageId,

		@Positive(message = "잘못된 아이디입니다")
		Long prevProfileImageId,

		String backGroundImageUrl,

		@Positive(message = "잘못된 아이디입니다")
		Long backGroundImageId,

		@Positive(message = "잘못된 아이디입니다")
		Long prevBackGroundImageId
) {
}
