package dev.kyudong.back.feed.api;

import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feed API의 명세를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스의 메소드들은 실제 코드에서 직접 호출되지 않지만,
 * Spring MVC 프레임워크에 의해 런타임 시 동적으로 구현체(Controller)와 연결되어 사용됩니다.
 * <p>
 * 따라서 IDE에서 '사용되지 않음(unused)'으로 잘못 분석할 수 있으므로,
 * {@code @SuppressWarnings("unused")}를 사용하여 관련 경고를 억제합니다.
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see SuppressWarnings
 */
@Tag(name = "Feed API", description = "사용자의 개인화된 콘텐츠 목록(피드) 관련 API 명세서")
public interface FeedApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자의 피드를 조회", description = "사용자의 팔로우를 기반으로 게시글을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글 조회",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = FeedDetailResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "lastFeedId": null,
									  "hasNext": false,
									  "content": [
										{
										  "postId": 96,
										  "userId": 279,
										  "subject": "게시글제목",
										  "content": "게시글본문입니다",
										  "status": "NORMAL",
										  "createdAt": "2025-08-22T15:34:36.089587",
										  "modifiedAt": "2025-08-22T15:34:36.089587"
										}
									  ]
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FeedDetailResDto> findFeeds(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "page", description = "마지막으로 조회한 피드의 아이디 (두 번째 페이지부터 사용됩니다)", example = "1")
			@RequestParam(required = false) int page,
			@CookieValue(name = GuestIdInterceptor.GUEST_ID_COOKIE_NAME, required = false) String guestId
	);

}
