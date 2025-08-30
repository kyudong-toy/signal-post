package dev.kyudong.back.common.jwt;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	@Override
	public boolean beforeHandshake(
			@NonNull ServerHttpRequest request,
			@NonNull ServerHttpResponse response,
			@NonNull WebSocketHandler wsHandler,
			@NonNull Map<String, Object> attributes
	) {
		log.debug("핸드세이크 시작");
		String token = resolveToken(request);

		if (token != null && jwtUtil.validateToken(token)) {
			String username = jwtUtil.getUsernameFromToken(token);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			attributes.put("principal", authenticationToken);

			log.info("WebSocket 핸드 셰이크가 성공했습니다: User {}", authenticationToken.getName());
			return true;
		}

		log.warn("WebSocket 핸드 셰이크가 실패했습니다: 토큰이 유효하지 않음");
		return false;
	}

	@Override
	public void afterHandshake(
			@NonNull ServerHttpRequest request,
			@NonNull ServerHttpResponse response,
			@NonNull WebSocketHandler wsHandler,
			@Nullable Exception exception) {
		log.debug("WebSocket 핸드세이크 종료");
	}

	private String resolveToken(ServerHttpRequest request) {
		String token = request.getURI().getQuery();
		if (token != null && token.startsWith("token=")) {
			return token.substring(6);
		}
		return null;
	}

}
