package dev.kyudong.back.common.stomp;

import org.springframework.messaging.simp.stomp.StompCommand;

import java.security.Principal;

public interface StompAuthorizationHandler {
	boolean supports(String destination);
	void checkAuthorization(Principal principal, String destination, StompCommand command);
}