package dev.kyudong.back.common.websocket;

import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BaseWebSocketHandler extends TextWebSocketHandler {

	private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

	protected WebSocketSession getSession(Long userId) {
		return sessions.get(userId);
	}

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
		Long userId = getUserId(session);
		if (userId == null) {
			log.warn("웹 소켓 연결에 실패했습니다: 사용자 인증 실패");
			session.close(CloseStatus.POLICY_VIOLATION.withReason("사용자 인증 실패"));
			return;
		}
		sessions.put(userId, session);
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		Long userId  = getUserId(session);
		if (userId != null) {
			sessions.remove(userId);
			log.info("웹 소켓 연결이 종료되었습니다: userId={} status={}", userId, status);
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
