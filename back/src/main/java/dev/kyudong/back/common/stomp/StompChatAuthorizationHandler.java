package dev.kyudong.back.common.stomp;

import dev.kyudong.back.chat.service.ChatMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompChatAuthorizationHandler implements StompAuthorizationHandler {

	private final ChatMemberService chatMemberService;
	private static final String DESTINATION_PREFIX = "/topic/chatroom/";

	@Override
	public boolean supports(String destination) {
		return destination != null && destination.startsWith(DESTINATION_PREFIX);
	}

	@Override
	public void checkAuthorization(Principal principal, String destination, StompCommand command) {
		Long roomId = Long.valueOf(destination.substring(DESTINATION_PREFIX.length()));

		if (command == StompCommand.SUBSCRIBE &&
				!chatMemberService.isChatMember(roomId, principal.getName())) {
			throw new AccessDeniedException("에러 처리 필요");
		}

		if (command == StompCommand.SEND &&
				!chatMemberService.isChatMember(roomId, principal.getName())) {
			throw new AccessDeniedException("에러 처리 필요");
		}
	}

}
