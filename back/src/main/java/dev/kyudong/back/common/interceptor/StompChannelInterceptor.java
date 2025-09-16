package dev.kyudong.back.common.interceptor;

import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.common.stomp.StompAuthorizationManager;
import dev.kyudong.back.common.stomp.StompPrperties;
import dev.kyudong.back.user.properties.UserTokenProperties;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

	private final StompAuthorizationManager checkAuthorization;
	private final JwtUtil jwtUtil;
	private final RedissonClient redissonClient;
	private final StompPrperties stompPrperties;
	private final UserTokenProperties userTokenProperties;

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
		String accessToken = accessor.getFirstNativeHeader("Authorization");

		if (!accessToken.startsWith("Bearer ")) {
			log.warn("STOMP CONNECT - Authorization 헤더가 없거나 형식이 잘못되었습니다.");
			throw new AuthenticationCredentialsNotFoundException("HEADER_INVALID");
		}

		String token = accessToken.substring(7);
		try {
			Claims claims = jwtUtil.getClaimsFromAccessToken(token);

			Principal principal = CustomUserPrincipal.createPrincipalFromClaims(claims);
			accessor.setUser(principal);

			String key =  stompPrperties.getPrefix() + accessor.getSessionId();
			RBucket<Principal> bucket = redissonClient.getBucket(key);
			bucket.set(principal, Duration.of(userTokenProperties.getAccessExpirationTime(), ChronoUnit.MILLIS));
			log.debug("STOMP 연결 - 웹 소켓 세션과 연결되었습니다: name={}", principal.getName());
		} catch (ExpiredJwtException e) {
			// 1. 토큰이 만료된 경우
			log.warn("STOMP CONNECT - 만료된 토큰입니다: {}", e.getMessage());
			throw new AuthenticationCredentialsNotFoundException("USER_TOKEN_EXPIRED");
		} catch (Exception e) {
			log.warn("STOMP CONNECT - 유효하지 않은 토큰입니다: {}", e.getMessage());
			throw new AuthenticationCredentialsNotFoundException("TOKEN_INVALID");
		}
	}

	private void handleSubscribe(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();

		String key =  stompPrperties.getPrefix() + accessor.getSessionId();
		RBucket<Principal> bucket = redissonClient.getBucket(key);
		Principal principal = bucket.get();

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
		String sessionId = accessor.getSessionId();
		String key = stompPrperties.getPrefix() + sessionId;

		RBucket<Principal> bucket = redissonClient.getBucket(key);
		Principal principal = bucket.get();
		bucket.delete();

		if (principal != null) {
			log.debug("STOMP DISCONNECT - Redis 세션 삭제 완료: key={}, principal={}", key, principal.getName());
		}
	}

}
