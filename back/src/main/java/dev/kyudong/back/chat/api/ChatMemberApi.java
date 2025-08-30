package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
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
 * ChatMember API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "ChatMember API", description = "채팅 사용자 API 명세서")
public interface ChatMemberApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자를 채팅방에 초대", description = "사용자를 채팅방에 초대 후 소켓으로 정보를 전송합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "사용자 초대 완료"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방입니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Room Not Found",
										"status": 404,
										"detail": "존재하지 않는 채팅방입니다: roomId=55",
										"instance": "/api/v1/chatroom/1/members/1",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "초대자가 채팅방에 존재하지 않습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Member Not Found",
										"status": 404,
										"detail": "채팅방에 존재하지 않는 사용자입니다: userId=1, chatroomId=1",
										"instance": "/api/v1/chatroom/1/members/1",
										"timestamp": "2025-08-16T05:03:26.747698400Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "초대할 사용자를 찾을 수 없습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Users Not Found",
										"status": 404,
										"detail": "요청한 사용자 목록을 조회할 수 없습니다",
										"instance": "/api/v1/chatroom/1/members/1",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<Void> inviteChatRoom(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(description = "초대할 채팅방의 아이디", required = true, example = "1")
			@PathVariable @Positive Long roomId,
			@RequestBody ChatMemberInviteReqDto requset
	);

	@SuppressWarnings("unused")
	@Operation(summary = "채팅방을 탈퇴합니다", description = "사용자가 채팅방을 떠나고 소켓으로 탈퇴한 사용자 정보를 전달합니다")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "선택한 채팅방을 떠납니다"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방입니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Room Not Found",
										"status": 404,
										"detail": "존재하지 않는 채팅방입니다: roomId=55",
										"instance": "/api/v1/chatroom/1/members/1",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "초대자가 채팅방에 존재하지 않습니다",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Chat Member Not Found",
										"status": 404,
										"detail": "채팅방에 존재하지 않는 사용자입니다: roomId=1, leaveUserId=1",
										"instance": "/api/v1/chatroom/1/members/me",
										"timestamp": "2025-08-16T05:03:26.747698400Z"
									}
									"""
							)
					)
			)
	})
	ResponseEntity<Void> leaveChatRoom(
			@Parameter(hidden = true, description = "로그인 사용자의 정보")
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@Parameter(description = "탈퇴할 채팅방의 아이디", required = true, example = "1")
			@PathVariable @Positive Long roomId
	);

}
