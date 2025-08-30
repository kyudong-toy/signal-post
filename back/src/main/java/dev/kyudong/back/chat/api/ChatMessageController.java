package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatMessageCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatMessageResDto;
import dev.kyudong.back.chat.service.ChatMessageService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatroom/{roomId}/message")
public class ChatMessageController implements ChatMessageApi {

	private final ChatMessageService chatMessageService;

	@Override
	@GetMapping
	public ResponseEntity<ChatMessageResDto> findMessages(
			@PathVariable @Positive Long roomId,
			@RequestParam(required = false) @Positive Long cursorId,
			@RequestParam(required = false) Instant cursorTime
	) {
		return ResponseEntity.ok(chatMessageService.findMessages(roomId, cursorId, cursorTime));
	}

	@Override
	@PostMapping
	public ResponseEntity<Void> createMessage(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long roomId,
			@RequestBody ChatMessageCreateReqDto request
	) {
		chatMessageService.createMessage(userPrincipal.getId(), roomId, request);
		return ResponseEntity.noContent().build();
	}

	@Override
	@DeleteMapping("{messageId}")
	public ResponseEntity<Void> deleteMessage(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long roomId,
			@PathVariable @Positive Long messageId
	) {
		chatMessageService.deleteMessage(userPrincipal.getId(), roomId, messageId);
		return ResponseEntity.noContent().build();
	}

}
