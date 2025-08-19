package dev.kyudong.back.post.api;

import dev.kyudong.back.post.api.dto.req.*;
import dev.kyudong.back.post.api.dto.res.*;
import dev.kyudong.back.post.service.CommentService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController implements CommentApi {

	private final CommentService commentService;

	@Override
	@GetMapping
	public ResponseEntity<List<CommentDetailResDto>> findCommentsByPostId(@PathVariable @Positive final Long postId) {
		return ResponseEntity.ok(commentService.findCommentsByPostId(postId));
	}

	@Override
	@PostMapping
	public ResponseEntity<CommentCreateResDto> createComment(
			@PathVariable @Positive final Long postId,
			@RequestBody @Valid CommentCreateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		CommentCreateResDto commentCreateResDto = commentService.createComment(postId, userPrincipal.getId(), request);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(commentCreateResDto.postId())
				.toUri();
		return ResponseEntity.created(uri).body(commentCreateResDto);
	}

	@Override
	@PatchMapping("/{commentId}")
	public ResponseEntity<CommentUpdateResDto> updateComment(
			@PathVariable @Positive final Long postId,
			@PathVariable @Positive final Long commentId,
			@RequestBody @Valid CommentUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(commentService.updateComment(postId, commentId, userPrincipal.getId(), request));
	}

	@Override
	@PatchMapping("/{commentId}/status")
	public ResponseEntity<CommentStatusUpdateResDto> updateCommentStatus(
			@PathVariable @Positive final Long postId,
			@PathVariable @Positive final Long commentId,
			@RequestBody @Valid CommentStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(commentService.updateCommentStatus(postId, commentId, userPrincipal.getId(), request));
	}

}
