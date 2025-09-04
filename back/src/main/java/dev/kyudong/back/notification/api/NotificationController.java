package dev.kyudong.back.notification.api;

import dev.kyudong.back.notification.api.dto.res.NotificationResDto;
import dev.kyudong.back.notification.service.NotificationService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController implements NotificationApi {

	private final NotificationService notificationService;

	@Override
	@GetMapping
	public ResponseEntity<NotificationResDto> findNotifications(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestParam(required = false) Long lastNotificationId,
			@RequestParam(defaultValue = "10") @Positive int size) {
		return ResponseEntity.ok(notificationService.findNotifications(userPrincipal.getId(), lastNotificationId, size));
	}

	@Override
	@PatchMapping("/{notificationId}")
	public ResponseEntity<Void> readNotification(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long notificationId) {
		notificationService.readNotification(userPrincipal.getId(), notificationId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@DeleteMapping("/{notificationId}")
	public ResponseEntity<Void> deleteNotification(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@PathVariable @Positive Long notificationId) {
		notificationService.deleteNotification(userPrincipal.getId(), notificationId);
		return ResponseEntity.noContent().build();
	}

}
