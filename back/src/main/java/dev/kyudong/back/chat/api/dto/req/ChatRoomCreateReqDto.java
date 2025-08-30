package dev.kyudong.back.chat.api.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record ChatRoomCreateReqDto(
		@Max(value = 100, message = "채팅방 이름의 최대 길이는 100글자 입니다")
		String roomname,

		@NotNull(message = "유저 아이디 목록은 필수입니다")
		Set<@NotNull(message = "사용자 이이디는 NULL이 올 수 없습니다") @Positive Long> userIds
) {
}
