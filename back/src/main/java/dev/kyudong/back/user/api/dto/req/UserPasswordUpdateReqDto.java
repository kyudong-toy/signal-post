package dev.kyudong.back.user.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateReqDto(
		@Size(min = 4, max = 150)
		@NotBlank(message = "PassWord can not be blank.")
		String password
) {
}
