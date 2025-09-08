package dev.kyudong.back.feed.api;

import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.service.FeedService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feeds")
public class FeedController implements FeedApi {

	private final FeedService feedService;

	@Override
	@GetMapping
	public ResponseEntity<FeedDetailResDto> findFeeds(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestParam(required = false, defaultValue = "0") int page,
			@CookieValue(name = GuestIdInterceptor.GUEST_ID_COOKIE_NAME, required = false) String guestId) {

		boolean isLoggedIn = userPrincipal != null;

		FeedDetailResDto response = isLoggedIn
				? feedService.findFeedsByUser(userPrincipal.getId(), page)
				: feedService.findFeedsWithGuset(guestId, page);

		return ResponseEntity.ok(response);
	}

}
