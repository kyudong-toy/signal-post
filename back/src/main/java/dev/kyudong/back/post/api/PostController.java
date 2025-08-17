package dev.kyudong.back.post.api;

import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController implements PostApi {

	private final PostService postService;

	@Override
	@GetMapping("/{postId}")
	public ResponseEntity<PostDetailResDto> findPostById(@PathVariable long postId) {
		return ResponseEntity.ok(postService.findPostById(postId));
	}

	@Override
	@PostMapping
	public ResponseEntity<PostCreateResDto> createUser(PostCreateReqDto request) {
		PostCreateResDto postCreateResDto = postService.createPost(request);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(postCreateResDto.postId())
				.toUri();
		return ResponseEntity.created(uri).body(postCreateResDto);
	}

	@Override
	@PatchMapping("/{postId}/update")
	public ResponseEntity<PostUpdateResDto> updatePost(@PathVariable long postId, PostUpdateReqDto request) {
		return ResponseEntity.ok(postService.updatePost(postId, request));
	}

	@Override
	@PatchMapping("/{postId}/status")
	public ResponseEntity<PostStatusUpdateResDto> updatePostStatus(@PathVariable long postId, PostStatusUpdateReqDto request) {
		return ResponseEntity.ok(postService.updatePostStatus(postId, request));
	}

}
