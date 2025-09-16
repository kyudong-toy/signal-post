package dev.kyudong.back.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GuestIdInterceptor implements HandlerInterceptor {

	public static final String GUEST_ID_COOKIE_NAME = "guestId";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Cookie[] cookies = request.getCookies();

		String guestId = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				// todo : 리프레쉬 키가 있다면 생략... 하지만 아직 없지
				if (cookieName.equals(GUEST_ID_COOKIE_NAME)) {
					guestId = cookie.getValue();
					break;
				}
			}
		}

		if (guestId == null) {
			String newGuestId = UUID.randomUUID().toString();
			ResponseCookie newCookie = ResponseCookie.from(GUEST_ID_COOKIE_NAME, newGuestId)
					.path("/")
					.httpOnly(true)
					.secure(true)
					.maxAge(Duration.ofDays(365)) // 1년
					.sameSite("None") // SameSite=None 속성을 명시적으로 추가
					.build();

			response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
		}

		request.setAttribute(GUEST_ID_COOKIE_NAME, guestId);

		return true;
	}

}
