package dev.kyudong.back.common.config;

import dev.kyudong.back.chat.websocket.ChatWebSocketHandler;
import dev.kyudong.back.common.jwt.JwtHandshakeInterceptor;
import dev.kyudong.back.notification.handler.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final NotificationWebSocketHandler notificationWebSocketHandler;
	private final ChatWebSocketHandler chatWebSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
				.addHandler(chatWebSocketHandler, "/ws/chat")
				.addInterceptors(jwtHandshakeInterceptor)
				.setAllowedOrigins("*");
	}

}
