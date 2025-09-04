package dev.kyudong.back.common.stomp;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthorizationManager {

	private final List<StompAuthorizationHandler> handlers;

	public void checkAuthorization(@NonNull Principal principal,
								   @NonNull String destination,
								   StompCommand command) {
		try {
			for (StompAuthorizationHandler handler : handlers) {
				if (handler.supports(destination)) {
					handler.checkAuthorization(principal, destination, command);
					return;
				}
			}
		} catch (Exception e) {
			log.error("에러");
			throw new RuntimeException();
		}
	}

}
