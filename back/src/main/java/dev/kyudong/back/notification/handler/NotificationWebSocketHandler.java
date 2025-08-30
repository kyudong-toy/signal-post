package dev.kyudong.back.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.websocket.BaseWebSocketHandler;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends BaseWebSocketHandler {

	private final ObjectMapper objectMapper;

	public void sendNotificationToUser(final Long userId, NotificationDetailResDto notificationDetailResDto) {
		log.debug("사용자에게 알림을 전송합니다: userId={}", userId);
		WebSocketSession session = super.getSession(userId);
		if (session != null && session.isOpen()) {
			try {
				String message = objectMapper.writeValueAsString(notificationDetailResDto);
				session.sendMessage(new TextMessage(message));
				log.info("사용자에게 알림을 전송했습니다: userId={}. message={}", userId, message);
			} catch (IOException e) {
				log.error("사용자에게 알림을 실패했습니다: userId={}", userId);
			}
		}
	}

}
