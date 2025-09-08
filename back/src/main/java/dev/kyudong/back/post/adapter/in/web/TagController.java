package dev.kyudong.back.post.adapter.in.web;

import dev.kyudong.back.post.application.port.in.web.TagUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController implements TagApi {

	private final TagUsecase tagUsecase;

	@Override
	@GetMapping("/search")
	public ResponseEntity<List<String>> findTagNamesByQuery(@RequestParam String query) {
		return ResponseEntity.ok(tagUsecase.findTagNamesByQuery(query));
	}

}
