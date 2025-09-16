package dev.kyudong.back.common.jwt;

import dev.kyudong.back.user.exception.UserTokenExpiredExcpetion;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final HandlerExceptionResolver handlerExceptionResolver;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		String token = resolveToken(request);

		if (token != null) {
			try {
				Claims claims = jwtUtil.getClaimsFromAccessToken(token);
				CustomUserPrincipal userPrincipal = CustomUserPrincipal.createPrincipalFromClaims(claims);

				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			} catch (UserTokenExpiredExcpetion u) {
				log.debug("토큰 인증시간이 만료되었습니다");
				handlerExceptionResolver.resolveException(request, response, null, u);
				return;
			} catch (Exception e) {
				log.error("토큰 검증 중 에러가 발생했습니다", e);
				handlerExceptionResolver.resolveException(request, response, null, e);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
