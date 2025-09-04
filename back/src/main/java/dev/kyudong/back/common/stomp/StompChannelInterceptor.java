package dev.kyudong.back.common.stomp;

import dev.kyudong.back.common.jwt.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

	private final StompAuthorizationManager checkAuthorization;
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	private final Map<String, Principal> sessions = new ConcurrentHashMap<>();

	@Override
	public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (accessor.getCommand() == null) {
			if (accessor.getHeader("simpMessageType") != null) {
				log.debug("메시지 타입 확인 : {}", accessor.getHeader("simpMessageType"));
			}
			return message;
		}

		switch (accessor.getCommand()) {
			case CONNECT -> handleConnect(accessor);
			case SUBSCRIBE -> handleSubscribe(accessor);
			case SEND -> handleSend(accessor);
			case DISCONNECT -> handleDisconnect(accessor);
			default -> {
			}
		}

		return message;
	}

	private void handleConnect(StompHeaderAccessor accessor) {
		String accessToekn = accessor.getFirstNativeHeader("Authorization").substring(7);
		if (jwtUtil.validateToken(accessToekn)) {
			String username = jwtUtil.getUsernameFromToken(accessToekn);
			Principal principal = (Principal) userDetailsService.loadUserByUsername(username);

			accessor.setUser(principal);
			sessions.put(accessor.getSessionId(), principal);
			log.debug("STOMP 연결 - 웹 소켓 세션과 연결되었습니다: name={}", principal.getName());
			return;
		}

		log.warn("STOMP CONNECT - 인증이 실패하였습니다");
		throw new AccessDeniedException("STOMP 사용시 인증이 필요합니다");
	}

	private void handleSubscribe(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		Principal principal = sessions.get(accessor.getSessionId());

		checkAuthorization.checkAuthorization(principal, destination, accessor.getCommand());
		log.debug("STOMP 구독 - 웹 소켓 세션이 구독되었습니다: name={}, destination={}", principal.getName(), destination);
	}

	private void handleSend(StompHeaderAccessor accessor) {
		Principal principal = accessor.getUser();
		String destination = accessor.getDestination();

		checkAuthorization.checkAuthorization(principal, destination, accessor.getCommand());
		log.debug("STOMP 구독 - 웹 소켓 세션에 메시지 발송되었습니다: name={}, destination={}", principal.getName(), destination);
	}

	private void handleDisconnect(StompHeaderAccessor accessor) {
		log.debug("STOMP 연결해제");
		Principal user = accessor.getUser();
		if (user != null) {
			log.info("STOMP DISCONNECT - User disconnected: {}", user.getName());
		} else {
			log.info("STOMP DISCONNECT - A user disconnected.");
		}
	}

}
