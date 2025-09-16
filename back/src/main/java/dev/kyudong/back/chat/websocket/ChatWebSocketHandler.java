package dev.kyudong.back.chat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.websocket.BaseWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public final class ChatWebSocketHandler extends BaseWebSocketHandler {

	private final ObjectMapper objectMapper;

	public <T> void sendChatMessage(final Long userId, ChatWebSocketMessage<T> payload) {
		log.debug("메시지를 전송합니다: userId={}", userId);
		WebSocketSession session = super.getSession(userId);
		if (session != null && session.isOpen()) {
			try {
				String message=  objectMapper.writeValueAsString(payload);
				session.sendMessage(new TextMessage(message));
				log.debug("메시지를 전송했습니다: userId={}. contents={}", userId, payload);
			} catch (JsonProcessingException j) {
				log.error("메시지 파싱에 실패했습니다: payload={}", payload, j);
			} catch (IOException i) {
				log.error("입출력 에러가 발생했습니다", i);
			} catch (Exception e) {
				log.error("에러가 발생했습니다", e);
			}
		}
	}

}
