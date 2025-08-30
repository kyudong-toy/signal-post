package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatMemberInviteReqDto;
import dev.kyudong.back.chat.service.ChatMemberService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatroom/{roomId}/members")
public class ChatMemberController implements ChatMemberApi {

	private final ChatMemberService chatMemberService;

	@PostMapping
	public ResponseEntity<Void> inviteChatRoom(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long roomId,
			@RequestBody ChatMemberInviteReqDto requset
	) {
		chatMemberService.inviteChatRoom(userPrincipal.getId(), roomId, requset);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> leaveChatRoom(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long roomId
	) {
		chatMemberService.leaveChatRoom(roomId, userPrincipal.getId());
		return ResponseEntity.noContent().build();
	}

}
