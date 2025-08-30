package dev.kyudong.back.chat.api.dto.res;

import dev.kyudong.back.chat.domain.ChatRoom;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;

public record ChatRoomResDto(
		Long lastChatroomId,
		Instant lastActivityAt,
		boolean hasNext,
		List<ChatRoomDetailResDto> content
) {
	public static ChatRoomResDto from(Slice<ChatRoom> chatrooms) {
		List<ChatRoomDetailResDto> content = chatrooms.getContent().stream()
				.map(ChatRoomDetailResDto::from)
				.toList();

		Long lastChatroomId = null;
		Instant lastActivityAt = null;
		boolean hasNext = chatrooms.hasNext();

		if (hasNext) {
			ChatRoom lastroom = chatrooms.getContent().get(chatrooms.getContent().size() - 1);
			lastChatroomId = lastroom.getId();
			lastActivityAt = lastroom.getLastActivityAt();
		}

		return new ChatRoomResDto(lastChatroomId, lastActivityAt, hasNext, content);
	}
}
