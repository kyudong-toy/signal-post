package dev.kyudong.back.chat.api.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ChatMemberInviteReqDto(
		@NotNull(message = "유저 아이디 목록은 필수입니다")
		@Size(max = 30, message = "최대 30명까지 한 번에 초대 가능합니다")
		Set<@NotNull(message = "사용자 이이디는 NULL이 올 수 없습니다") @Positive Long> userIds
) {
}
