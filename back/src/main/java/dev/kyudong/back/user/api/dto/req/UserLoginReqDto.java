package dev.kyudong.back.user.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginReqDto(
		@Size(min = 4, max = 30)
		@NotBlank(message = "UserName can not be blank.")
		String username,

		@Size(min = 4, max = 150)
		@NotBlank(message = "PassWord can not be blank.")
		String password
) {
}
