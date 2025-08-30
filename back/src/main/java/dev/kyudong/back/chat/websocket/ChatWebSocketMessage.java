package dev.kyudong.back.chat.websocket;

import dev.kyudong.back.chat.event.ChatEventType;

public record ChatWebSocketMessage<T>(
		ChatEventType type,
		T payload
) {
	public static <T> ChatWebSocketMessage<T> of(ChatEventType type, T payload) {
		return new ChatWebSocketMessage<>(type, payload);
	}
}
