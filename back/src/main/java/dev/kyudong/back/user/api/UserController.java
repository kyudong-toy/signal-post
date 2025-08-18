package dev.kyudong.back.user.api;

import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.UserCreateResDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserStatusUpdateResDto;
import dev.kyudong.back.user.api.dto.res.UserUpdateResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import dev.kyudong.back.user.service.UserService;
import jakarta.validation.Valid;
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
	@PostMapping("/login")
	public ResponseEntity<UserLoginResDto> loginUser(@RequestBody @Valid UserLoginReqDto request) {
		return ResponseEntity.ok(userService.loginUser(request));
	}

	@Override
	@PatchMapping("/me/update")
	public ResponseEntity<UserUpdateResDto> updateUser(
			@RequestBody @Valid UserUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(userService.updateUser(userPrincipal.getId(), request));
	}

	@Override
	@PatchMapping("/me/status")
	public ResponseEntity<UserStatusUpdateResDto> updateUserStatus(
			@RequestBody @Valid UserStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(userService.updateUserStatus(userPrincipal.getId(), request));
	}

}
