package dev.kyudong.back.post.adapter.in.web;

import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
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

	private final PostUsecase postUsecase;

	@Override
	@GetMapping("/{postId}")
	public ResponseEntity<PostDetailResDto> findPostById(
			@PathVariable @Positive Long postId,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@CookieValue(name = GuestIdInterceptor.GUEST_ID_COOKIE_NAME, required = false) String guestId) {

		boolean isLoggedIn = userPrincipal != null;

		PostDetailResDto response = isLoggedIn
				? postUsecase.findPostByIdWithUser(userPrincipal.getId(), postId)
				: postUsecase.findPostByIdWithGuest(guestId, postId);

		return ResponseEntity.ok(response);
	}

	@Override
	@PostMapping
	public ResponseEntity<PostCreateResDto> createUser(
			@RequestBody @Valid PostCreateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		PostCreateResDto postCreateResDto = postUsecase.createPost(userPrincipal.getId(), request);
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
		return ResponseEntity.ok(postUsecase.updatePost(postId, userPrincipal.getId(), request));
	}

	@Override
	@PatchMapping("/{postId}/status")
	public ResponseEntity<PostStatusUpdateResDto> updatePostStatus(
			@PathVariable @Positive Long postId,
			@RequestBody @Valid PostStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(postUsecase.updatePostStatus(postId, userPrincipal.getId(), request));
	}

}
