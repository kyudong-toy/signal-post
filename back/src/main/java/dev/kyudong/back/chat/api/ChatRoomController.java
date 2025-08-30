package dev.kyudong.back.chat.api;

import dev.kyudong.back.chat.api.dto.req.ChatRoomCreateReqDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomCreateResDto;
import dev.kyudong.back.chat.api.dto.res.ChatRoomResDto;
import dev.kyudong.back.chat.service.ChatRoomService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatroom")
public class ChatRoomController implements ChatRoomApi {

	private final ChatRoomService chatRoomService;

	@Override
	@GetMapping
	public ResponseEntity<ChatRoomResDto> findChatroom(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestParam(required = false) @Positive Long lastChatroomId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursorTime) {
		return ResponseEntity.ok(chatRoomService.findChatRooms(userPrincipal.getId(), lastChatroomId, cursorTime));
	}

	@Override
	@PostMapping
	public ResponseEntity<ChatRoomCreateResDto> createChatroom(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestBody ChatRoomCreateReqDto request) {
		ChatRoomCreateResDto chatRoomCreateResDto = chatRoomService.createChatRoom(userPrincipal.getId(), request);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{roomId}")
				.buildAndExpand(chatRoomCreateResDto.roomId())
				.toUri();
		return ResponseEntity.created(uri).body(chatRoomCreateResDto);
	}

}

