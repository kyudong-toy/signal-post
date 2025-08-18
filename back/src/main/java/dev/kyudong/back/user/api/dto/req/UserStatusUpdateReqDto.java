package dev.kyudong.back.user.api.dto.req;

import dev.kyudong.back.user.domain.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserStatusUpdateReqDto(
		@Size(min = 4, max = 150)
		@NotBlank(message = "PassWord can not be blank.")
		String password,

		@NotNull(message = "UserStatus can not be null.")
		UserStatus userStatus
) {
}
