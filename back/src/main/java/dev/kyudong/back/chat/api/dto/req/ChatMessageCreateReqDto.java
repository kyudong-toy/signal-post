package dev.kyudong.back.chat.api.dto.req;

import dev.kyudong.back.chat.domain.MessageType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record ChatMessageCreateReqDto(
		@Max(value = 1000, message = "최대 콘텐츠 길이는 1000글자 입니다")
		String content,

		@NotNull(message = "메시지 유형은 필수입니다")
		MessageType messageType
) {
}
