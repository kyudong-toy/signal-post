package dev.kyudong.back.feed.api;

import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.service.FeedService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feeds")
public class FeedController implements FeedApi {

	private final FeedService feedService;

	@Override
	@GetMapping
	public ResponseEntity<FeedDetailResDto> findFeeds(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestParam(required = false) Long lastFeedId,
			@RequestParam(defaultValue = "30") @Positive int size) {
		return ResponseEntity.ok(feedService.findFeeds(userPrincipal.getId(), lastFeedId, size));
	}

}
