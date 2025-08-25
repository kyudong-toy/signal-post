package dev.kyudong.back.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.notification.api.dto.res.NotificationDetailResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

	private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Long userId = getUserId(session);
		if (userId == null) {
			log.warn("웹 소켓 연결에 실패했습니다: 사용자 인증 실패");
			session.close(CloseStatus.POLICY_VIOLATION.withReason("사용자 인증 실패"));
			return;
		}
		sessions.put(userId, session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Long userId  = getUserId(session);
		if (userId != null) {
			sessions.remove(userId);
			log.info("웹 소켓 연결이 종료되었습니다: userId={} status={}", userId, status);
		}
	}

	public void sendMessageToUser(final Long userId, NotificationDetailResDto notificationDetailResDto) {
		log.debug("메시지를 전송을 시작합니다: userId={}", userId);
		WebSocketSession session = sessions.get(userId);
		if (session != null && session.isOpen()) {
			try {
				String message = objectMapper.writeValueAsString(notificationDetailResDto);
				session.sendMessage(new TextMessage(message));
				log.info("메시지 전송를 전송했습니다: userId={}. message={}", userId, message);
			} catch (IOException e) {
				log.error("메시지 전달에 실패했습니다: userId={}", userId);
			}
		}
	}

	private Long getUserId(WebSocketSession session) {
		if (session.getPrincipal() instanceof Authentication authentication) {
			if (authentication.getPrincipal() instanceof CustomUserPrincipal) {
				return ((CustomUserPrincipal) authentication.getPrincipal()).getId();
			}
		}
		return null;
	}

}
