package dev.kyudong.back.post.adapter.in.web;

import dev.kyudong.back.post.application.port.in.web.CommentUsecase;
import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
import dev.kyudong.back.post.domain.entity.CommentSort;
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

	private final CommentUsecase commentUsecase;

	@Override
	@GetMapping
	public ResponseEntity<CommentListResDto> findCommentsByPostId(
			@PathVariable @Positive final Long postId,
			@RequestParam(required = false) @Positive Long cursorId,
			@RequestParam(required = false) CommentSort sort
	) {
		return ResponseEntity.ok(commentUsecase.findComments(postId, cursorId, sort));
	}

	@Override
	@PostMapping
	public ResponseEntity<CommentCreateResDto> createComment(
			@PathVariable @Positive final Long postId,
			@RequestBody @Valid CommentCreateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		CommentCreateResDto commentCreateResDto = commentUsecase.createComment(postId, userPrincipal.getId(), request);
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
		return ResponseEntity.ok(commentUsecase.updateComment(postId, commentId, userPrincipal.getId(), request));
	}

	@Override
	@PatchMapping("/{commentId}/status")
	public ResponseEntity<CommentStatusUpdateResDto> updateCommentStatus(
			@PathVariable @Positive final Long postId,
			@PathVariable @Positive final Long commentId,
			@RequestBody @Valid CommentStatusUpdateReqDto request,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
		return ResponseEntity.ok(commentUsecase.updateCommentStatus(postId, commentId, userPrincipal.getId(), request));
	}

}
