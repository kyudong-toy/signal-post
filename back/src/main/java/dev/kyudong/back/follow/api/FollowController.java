package dev.kyudong.back.follow.api;

import dev.kyudong.back.follow.api.res.*;
import dev.kyudong.back.follow.service.FollowService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{username}")
public class FollowController implements FollowApi {

	private final FollowService followService;

	@Override
	@GetMapping("/followers")
	public ResponseEntity<List<FollowerDetailResDto>> findFollowers(
			@PathVariable String username) {
		return ResponseEntity.ok(followService.findFollowers(username));
	}

	@Override
	@GetMapping("/following")
	public ResponseEntity<List<FollowingDetailResDto>> findFollowings(
			@PathVariable String username) {
		return ResponseEntity.ok(followService.findFollowings(username));
	}

	@Override
	@PostMapping("/follow")
	public ResponseEntity<FollowCreateResDto> createFollow(
			@PathVariable String username,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		FollowCreateResDto followCreateResDto = followService.createFollow(userPrincipal.getId(), username);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.build()
				.toUri();
		return ResponseEntity.created(uri).body(followCreateResDto);
	}

	@Override
	@PatchMapping("/accept")
	public ResponseEntity<FollowAcceptResDto> acceptFollow(
			@PathVariable String username,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(followService.acceptFollow(userPrincipal.getId(), username));
	}

	@Override
	@PatchMapping("/block")
	public ResponseEntity<FollowBlokcedResDto> blokcFollow(
			@PathVariable String username,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(followService.blokcFollow(userPrincipal.getId(), username));
	}

	@Override
	@DeleteMapping("/follow")
	public ResponseEntity<FollowDeleteResDto> deleteFollow(
			@PathVariable String username,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(followService.deleteFollow(userPrincipal.getId(), username))	;
	}

}
