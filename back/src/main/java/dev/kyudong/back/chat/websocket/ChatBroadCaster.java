package dev.kyudong.back.chat.websocket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBroadCaster {

	private final ChatWebSocketHandler chatWebSocketHandler;

	/**
	 * 지정된 사용자 ID 목록(채팅방 참여자)에게 동일한 메시지를 비동기적으로 브로드캐스팅합니다.
	 * <p>
	 * 이 메소드는 {@link org.springframework.scheduling.annotation.Async @Async}로 동작하므로,
	 * 호출 즉시 반환되며 실제 메시지 전송은 별도의 스레드 풀에서 수행됩니다.
	 *
	 * @param users                메시지를 수신할 사용자 ID의 Set
	 * @param webSocketMessage     전송할 메시지 ({@link ChatWebSocketMessage}).
	 * @see ChatWebSocketHandler#sendChatMessage(Long, ChatWebSocketMessage)
	 */
	@Async
	public <T> void brodCastMessageToMembers(@NonNull Set<Long> users, @NonNull ChatWebSocketMessage<T> webSocketMessage) {
		log.debug("{}명에게 메시지를 전송합니다", users.size());

		users.forEach(userId -> chatWebSocketHandler.sendChatMessage(userId, webSocketMessage));

		log.debug("메시지 전송이 완료되었습니다");
	}

	/**
	 * 메시지 목록을 서로다른 내용의 메시지를 비동기적으로 브로드캐스팅합니다.
	 * <p>
	 * 이 메소드는 {@link org.springframework.scheduling.annotation.Async @Async}로 동작하므로,
	 * 호출 즉시 반환되며 실제 메시지 전송은 별도의 스레드 풀에서 수행됩니다.
	 *
	 * @param webSocketMessage     전송할 메시지 ({@link ChatWebSocketMessage}).
	 * @see ChatWebSocketHandler#sendChatMessage(Long, ChatWebSocketMessage)
	 */
	@Async
	public <T> void brodCastIndividualMessages(@NonNull Map<Long, ChatWebSocketMessage<T>> webSocketMessage) {
		log.debug("{}명에게 개별 메시지를 전송합니다", webSocketMessage.size());

		webSocketMessage.forEach(chatWebSocketHandler::sendChatMessage);

		log.debug("개별 메시지 전송이 완료되었습니다");
	}

}
