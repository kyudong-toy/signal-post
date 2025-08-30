package dev.kyudong.back.chat.api.dto.res;

import dev.kyudong.back.chat.domain.ChatMessage;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;

public record ChatMessageResDto(
		Long cursorId,
		Instant cursorTime,
		boolean hasNext,
		List<ChatMessageDetailResDto> content
) {
	public static ChatMessageResDto from(Slice<ChatMessage> chatMessages) {
		List<ChatMessageDetailResDto> content = chatMessages.getContent().stream()
				.map(ChatMessageDetailResDto::from)
				.toList();

		Long cursorId = null;
		Instant cursorTime = null;
		boolean hasNext = chatMessages.hasNext();

		if (hasNext) {
			ChatMessage lastroom = chatMessages.getContent().get(chatMessages.getContent().size() - 1);
			cursorId = lastroom.getId();
			cursorTime = lastroom.getCreatedAt();
		}

		return new ChatMessageResDto(cursorId, cursorTime, hasNext, content);
	}
}
