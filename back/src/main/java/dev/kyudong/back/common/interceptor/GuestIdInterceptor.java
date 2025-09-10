package dev.kyudong.back.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GuestIdInterceptor implements HandlerInterceptor {

	public static final String GUEST_ID_COOKIE_NAME = "guestId";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Cookie[] cookies = request.getCookies();
		boolean isFirstTime = true;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				// todo : 리프레쉬 키가 있다면 생략... 하지만 아직 없지
				if (cookieName.equals(GUEST_ID_COOKIE_NAME)) {
					isFirstTime = false;
					break;
				}
			}
		}

		if (isFirstTime) {
			String newGuestId = UUID.randomUUID().toString();
			Cookie newCookie = new Cookie(GUEST_ID_COOKIE_NAME, newGuestId);
			newCookie.setPath("/");
			newCookie.setHttpOnly(true);
			newCookie.setSecure(true);
			newCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
			response.addCookie(newCookie);
		}

		return true;
	}

}
