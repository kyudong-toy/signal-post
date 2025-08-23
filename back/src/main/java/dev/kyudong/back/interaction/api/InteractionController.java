package dev.kyudong.back.interaction.api;

import dev.kyudong.back.interaction.api.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.service.InteractionService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interaction")
public class InteractionController implements InteractionApi {

	private final InteractionService interactionService;

	@Override
	@PostMapping("/{targetType}/{targetId}")
	public ResponseEntity<InteractionResDto> doInteraction(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable TargetType targetType,
			@PathVariable @Positive Long targetId,
			@RequestBody InteractionReqDto request) {
		return ResponseEntity.ok(interactionService.doInteraction(userPrincipal.getId(), targetType, targetId, request));
	}

	@Override
	@DeleteMapping("/{targetType}/{targetId}")
	public ResponseEntity<Void> deleteInteraction(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable TargetType targetType,
			@PathVariable Long targetId) {
		interactionService.deleteInteraction(userPrincipal.getId(), targetType, targetId);
		return ResponseEntity.noContent().build();
	}

}
