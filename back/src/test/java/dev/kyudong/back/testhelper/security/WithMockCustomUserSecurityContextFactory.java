package dev.kyudong.back.testhelper.security;

import dev.kyudong.back.user.security.CustomUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		CustomUserPrincipal principal = CustomUserPrincipal.builder()
				.id(customUser.id())
				.username(customUser.username())
				.status(customUser.status())
				.role(customUser.role())
				.build();

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

		context.setAuthentication(authentication);
		return context;
	}

}