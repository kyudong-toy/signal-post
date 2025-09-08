package dev.kyudong.back.interaction.api;

import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.interaction.api.dto.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.dto.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.TargetType;
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
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interaction API의 명세를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스의 메소드들은 실제 코드에서 직접 호출되지 않지만,
 * Spring MVC 프레임워크에 의해 런타임 시 동적으로 구현체(Controller)와 연결되어 사용됩니다.
 * <p>
 * 따라서 IDE에서 '사용되지 않음(unused)'으로 잘못 분석할 수 있으므로,
 * {@code @SuppressWarnings("unused")}를 사용하여 관련 경고를 억제합니다.
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see SuppressWarnings
 * @see dev.kyudong.back.interaction.domain.TargetType
 */
@Tag(name = "Interaction API", description = "사용자와 콘텐츠간의 상호관련 관련 API 명세서")
public interface InteractionApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자와 대상 상호작용", description = "사용자가 선택한 대상과 상호작용을 합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "상호작용 성공",
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
			@ApiResponse(responseCode = "404", description = "해당 대상과 상호작용한 적이 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Interaction Not Found",
										"status": 404,
										"detail": "상호작용한 기록이 없습니다: userId=999, targetId=999"",
										"instance": "/api/v1/interaction/POST/999",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<InteractionResDto> doInteraction(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "targetType", description = "상호작용할 대상의 타입")
			@PathVariable TargetType targetType,
			@Parameter(name = "targetId", description = "상호작용할 대상의 고유값(Id)")
			@PathVariable @Positive Long targetId,
			@RequestBody InteractionReqDto request
	);

	@SuppressWarnings("unused")
	@Operation(summary = "사용자와 대상의 상호작용 취소", description = "사용자와 대상의 상호작용을 취소(삭제)합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "사용자와 대상의 상호작용을 취소(삭제)합니다"),
			@ApiResponse(responseCode = "404", description = "해당 대상과 상호작용한 적이 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Interaction Not Found",
										"status": 404,
										"detail": "상호작용한 기록이 없습니다: userId=999, targetId=999"",
										"instance": "/api/v1/interaction/POST/999",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<Void> deleteInteraction(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(name = "targetType", description = "상호작용할 대상의 타입")
			@PathVariable TargetType targetType,
			@Parameter(name = "targetId", description = "상호작용할 대상의 고유값(Id)")
			@PathVariable @Positive Long targetId
	);

}
