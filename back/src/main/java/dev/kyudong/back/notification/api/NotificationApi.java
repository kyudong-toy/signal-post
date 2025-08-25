package dev.kyudong.back.notification.api;

import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Notification API의 명세를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스의 메소드들은 실제 코드에서 직접 호출되지 않지만,
 * Spring MVC 프레임워크에 의해 런타임 시 동적으로 구현체(Controller)와 연결되어 사용됩니다.
 * <p>
 * 따라서 IDE에서 '사용되지 않음(unused)'으로 잘못 분석할 수 있으므로,
 * {@code @SuppressWarnings("unused")}를 사용하여 관련 경고를 억제합니다.
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see java.lang.SuppressWarnings
 */
@Tag(name = "Notification API", description = "알림 관련 API 명세서")
public interface NotificationApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자의 알림 목록을 조회", description = "사용자의 알림 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "알림 목록 조회",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = NotificationResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "lastFeedId": null,
									  "hasNext": false,
									  "content": [
										{
										  "id": 1,
										  "receiverId": 1,
										  "senderId": 2,
										  "redirectUrl": "/post/1",
										  "status": "POST",
										  "createdAt": "2025-08-22T15:34:36.089587",
										}
									  ]
									}
									"""
							)
					)
			),
	})
	ResponseEntity<NotificationResDto> findCommentsByPostId(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "lastFeedId", description = "마지막으로 조회한 알림의 아이디 (두 번째 페이지부터 사용됩니다)", example = "1")
			@RequestParam(required = false) Long lastFeedId,
			@Parameter(name = "size", description = "한 페이지에 불러올 알림 개수", example = "10")
			@RequestParam(defaultValue = "10") @Positive int size
	);

	@SuppressWarnings("unused")
	@Operation(summary = "사용자의 알림을 조회", description = "사용자의 알림을 아이디를 이용해 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "사용자의 알림을 아이디를 이용해 조회합니다"),
			@ApiResponse(responseCode = "404", description = "조회한 알림이 존재하지 않습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Notification Not Found",
										"status": 404,
										"detail": "조회를 요청한 알림이 존재하지 않습니다: notificationId=1",
										"instance": "/api/v1/notification/1",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<Void> readNotification(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "notificationId", description = "조회할 알림 고유 아이디")
			@PathVariable @Positive Long notificationId
	);

	@SuppressWarnings("unused")
	@Operation(summary = "사용자의 알림을 삭제", description = "사용자의 알림을 아이디를 이용해 삭제합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "사용자의 알림을 아이디를 이용해 삭제합니다")
	})
	ResponseEntity<Void> deleteNotification(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "notificationId", description = "조회할 알림 고유 아이디")
			@PathVariable @Positive Long notificationId
	);

}
