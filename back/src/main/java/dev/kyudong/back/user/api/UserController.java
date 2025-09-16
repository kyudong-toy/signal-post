package dev.kyudong.back.user.api;

import dev.kyudong.back.user.api.dto.req.*;
import dev.kyudong.back.user.api.dto.res.*;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import dev.kyudong.back.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

	private final UserService userService;

	@Override
	@GetMapping("/{username}")
	public ResponseEntity<UserDetailResDto> findUser(
			@PathVariable @NotBlank String username,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		UserDetailResDto response;
		if (userPrincipal == null) {
			response = userService.findUser(username, null);
		} else {
			response = userService.findUser(username, userPrincipal.getId());
		}
		return ResponseEntity.ok(response);
	}

	@Override
	@PostMapping
	public ResponseEntity<UserCreateResDto> createUser(@RequestBody @Valid UserCreateReqDto request) {
		UserCreateResDto userCreateResDto = userService.createUser(request);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(userCreateResDto.id())
				.toUri();
		return ResponseEntity.created(uri).body(userCreateResDto);
	}

	@Override
	@PatchMapping("/me/update")
	public ResponseEntity<UserProfileUpdateResDto> updateProfile(
			@RequestBody @Valid UserProfileUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(userService.updateProfile(userPrincipal.getUsername(), request));
	}

	@Override
	@PatchMapping("/me/password/update")
	public ResponseEntity<UserPasswordUpdateResDto> updatePassword(
			@RequestBody @Valid UserPasswordUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(userService.updatePassword(userPrincipal.getUsername(), request));
	}

	@Override
	@PatchMapping("/me/status")
	public ResponseEntity<UserStatusUpdateResDto> updateUserStatus(
			@RequestBody @Valid UserStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(userService.updateUserStatus(userPrincipal.getUsername(), request));
	}

}
