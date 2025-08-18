package dev.kyudong.back.post.api;

import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.service.PostService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController implements PostApi {

	private final PostService postService;

	@Override
	@GetMapping("/{postId}")
	public ResponseEntity<PostDetailResDto> findPostById(@PathVariable @Positive Long postId) {
		return ResponseEntity.ok(postService.findPostById(postId));
	}

	@Override
	@PostMapping
	public ResponseEntity<PostCreateResDto> createUser(
			@RequestBody @Valid PostCreateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		PostCreateResDto postCreateResDto = postService.createPost(userPrincipal.getId(), request);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(postCreateResDto.postId())
				.toUri();
		return ResponseEntity.created(uri).body(postCreateResDto);
	}

	@Override
	@PatchMapping("/{postId}/update")
	public ResponseEntity<PostUpdateResDto> updatePost(
			@PathVariable @Positive Long postId,
			@RequestBody @Valid PostUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(postService.updatePost(postId, userPrincipal.getId(), request));
	}

	@Override
	@PatchMapping("/{postId}/status")
	public ResponseEntity<PostStatusUpdateResDto> updatePostStatus(
			@PathVariable @Positive Long postId,
			@RequestBody @Valid PostStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(postService.updatePostStatus(postId, userPrincipal.getId(), request));
	}

}
