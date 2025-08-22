package dev.kyudong.back.follow.api;

import dev.kyudong.back.follow.api.res.*;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Follow API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "Follow API", description = "팔로우 관련 API 명세서")
public interface FollowApi {

	@SuppressWarnings("unused")
	@Operation(summary = "팔로우 목록 조회", description = "대상(username)의 팔로우 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "팔로우 요청 성공"),
			@ApiResponse(responseCode = "404", description = "팔로워 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			)
	})
	ResponseEntity<List<FollowerDetailResDto>> findFollowers(@PathVariable String username);

	@SuppressWarnings("unused")
	@Operation(summary = "팔로잉 목록 조회", description = "대상(username)의 팔로잉 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "팔로앙 요청 성공"),
			@ApiResponse(responseCode = "404", description = "팔로잉 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			)
	})
	ResponseEntity<List<FollowingDetailResDto>> findFollowings(@PathVariable String username);

	@SuppressWarnings("unused")
	@Operation(summary = "팔로우 생성 (팔로우 요청)", description = "대상(username)에게 팔로우 요청을 합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "팔로우 요청 성공"),
			@ApiResponse(responseCode = "404", description = "팔로잉 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "400", description = "이미 팔로잉 요청을 한 사용자임.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Already Follow Relation",
										"status": 400,
										"detail": "이미 팔로잉 중 입니다: followerId=999, followingUsername=testUser",
										"instance": "/api/v1/users/testUser/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FollowCreateResDto> createFollow(
			@PathVariable String username,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

	@SuppressWarnings("unused")
	@Operation(summary = "팔로우 승낙", description = "요청한 팔로우를 승낙합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "팔로우 승낙함"),
			@ApiResponse(responseCode = "404", description = "팔로잉 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "400", description = "이미 팔로잉 요청을 한 사용자임.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Already Follow Relation",
										"status": 400,
										"detail": "이미 팔로잉 중 입니다: followerId=999, followingUsername=testUser",
										"instance": "/api/v1/users/testUser/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FollowAcceptResDto> acceptFollow(
			@PathVariable String username,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

	@SuppressWarnings("unused")
	@Operation(summary = "팔로우 해제", description = "요청한 팔로우를 해제합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "팔로우 승낙함"),
			@ApiResponse(responseCode = "404", description = "팔로잉 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "400", description = "이미 팔로잉 요청을 한 사용자임.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Already Follow Relation",
										"status": 400,
										"detail": "이미 팔로잉 중 입니다: followerId=999, followingUsername=testUser",
										"instance": "/api/v1/users/testUser/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FollowBlokcedResDto> blokcFollow(
			@PathVariable String username,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

	@SuppressWarnings("unused")
	@Operation(summary = "팔로우 제거", description = "언팔로잉 합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "언팔로잉 요청 완료"),
			@ApiResponse(responseCode = "404", description = "팔로잉 사용자가 존재하지 않음.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: testUser Not Found",
										"instance": "/api/v1/users/{username}/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "400", description = "팔로잉 요청이 되어있지 않음",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Already Follow Relation",
										"status": 400,
										"detail": "이미 팔로잉 중 입니다: followerId=999, followingUsername=testUser",
										"instance": "/api/v1/users/testUser/follow",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FollowDeleteResDto> deleteFollow(
			@PathVariable String username,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

}
