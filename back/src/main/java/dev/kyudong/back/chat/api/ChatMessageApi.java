package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

/**
 * ChatMessage API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "ChatMessage API", description = "채팅 메시지 API 명세서")
public interface ChatMessageApi {

	@SuppressWarnings("unused")
	@Operation(summary = "채팅 메시지 조회", description = "채팅방의 메시지를 조회합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ChatMessageResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "cursorId": 1,
									  "cursorTime": "2025-08-22T15:34:36.089587",
									  "hasNext": false,
									  "content": [
										{
										  "messageId": 96,
										  "senderId": 279,
										  "content": "게시글본문입니다",
										  "messageType": "TEXT",
										  "messageStatus": "ACTIVE",
										  "createdAt": "2025-08-22T15:34:36.089587"
										}
									  ]
									}
									"""
							)
					)
			),
	})
	ResponseEntity<ChatMessageResDto> findMessages(
			@Parameter(description = "채팅방 아이디")
			@PathVariable @Positive Long roomId,
			@Parameter(name = "cursorId", description = "마지막 메시지 아이디")
			@RequestParam(required = false) @Positive Long cursorId,
			@Parameter(name = "cursorTime", description = "마지막 채팅방 메시지 시간")
			@RequestParam(required = false) Instant cursorTime
	);

	@SuppressWarnings("unused")
	@Operation(summary = "채팅 메시지 생성", description = "채팅 메시지를 생성하고 전송하고 소켓으로 전송합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "채팅 메시지 성공"),
			@ApiResponse(responseCode = "404", description = "메시지를 전송한 방이 존재하지 않습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Room Not Found",
										"status": 404,
										"detail": "존재하지 않는 채팅방입니다: roomId=1",
										"instance": "/api/v1/chatroom/1/message",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "메시지를 전송한 사용자가 채팅방에 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Member Not Found",
										"status": 404,
										"detail": "채팅방에 존재하지 않는 사용자입니다: userId=1, chatroomId=1",
										"instance": "/api/v1/chatroom/1/message",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<Void> createMessage(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(description = "채팅방 아이디")
			@PathVariable @Positive Long roomId,
			@RequestBody ChatMessageCreateReqDto request
	);

	@SuppressWarnings("unused")
	@Operation(summary = "메시지를 삭제", description = "선택한 메시지를 삭제하고 소켓으로 전송합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "선택한 메시지를 삭제합니다"),
			@ApiResponse(responseCode = "404", description = "메시지를 전송한 방이 존재하지 않습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Room Not Found",
										"status": 404,
										"detail": "존재하지 않는 채팅방입니다: roomId=1",
										"instance": "/api/v1/chatroom/1/message",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "메시지를 전송한 사용자가 채팅방에 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Member Not Found",
										"status": 404,
										"detail": "채팅방에 존재하지 않는 사용자입니다: userId=1, chatroomId=1",
										"instance": "/api/v1/chatroom/1/message",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "삭제할 메시지를 찾을 수 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Message Not Found",
										"status": 404,
										"detail": "존재하지 않는 메시지입니다: messageId=1, chatroomId=1,
										"instance": "/api/v1/chatroom/1/message/1",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<Void> deleteMessage(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(description = "채팅방 아이디")
			@PathVariable @Positive Long roomId,
			@Parameter(description = "삭제할 메시지 아이디")
			@PathVariable @Positive Long messageId
	);

}
