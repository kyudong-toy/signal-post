package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

/**
 * ChatRoomApi API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "ChatRoomApi API", description = "채팅방 API 명세서")
public interface ChatRoomApi {

	@SuppressWarnings("unused")
	@Operation(summary = "채팅방 목록 조회", description = "채팅방 목록을 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "채팅방 목록 조회 완료",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = NotificationResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "lastChatroomId": 1,
									  "lastActivityAt": "2025-08-16T16:20:22.367368100Z",
									  "hasNext": false,
									  "content": [
										{
										  "roomId": 1,
										  "memberCount": 1
										}
									  ]
									}
									"""
							)
					)
			)
	})
	ResponseEntity<ChatRoomResDto> findChatroom(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(description = "마지막 조회한 채팅방 아이디")
			@RequestParam(required = false) @Positive Long lastChatroomId,
			@Parameter(description = "채팅방 마지막 활성화 시간")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			Instant cursorTime
	);

	@SuppressWarnings("unused")
	@Operation(summary = "채팅방을 생성", description = "채팅방을 생성합니다")
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "채팅방 생성 완료",
					headers = @Header(
							name = "Location",
							description = "생성된 채팅방의 리소스의 URI",
							schema = @Schema(type = "string", format = "uri"),
							examples = @ExampleObject(value = "/api/v1/chatroom/1")
					),
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = NotificationResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "roomId": 1,
									  "memberCount": 1,
									  "status": 'ACTIVE',
									  "createdAt": "2025-08-16T16:20:22.367368100Z",
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "요청한 사용자 목록이 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Users Not Found",
										"status": 404,
										"detail": "요청한 사용자 목록을 조회할 수 없습니다",
										"instance": "/api/v1/chatroom",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<ChatRoomCreateResDto> createChatroom(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestBody ChatRoomCreateReqDto request
	);

}
