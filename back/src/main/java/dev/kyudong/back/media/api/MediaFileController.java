package dev.kyudong.back.media.api;

import dev.kyudong.back.media.api.dto.req.MediaFileUploadCompleteReqDto;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadResDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadStartResDto;
import dev.kyudong.back.media.service.MediaFileService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class MediaFileController implements MediaFileApi {

	private final MediaFileService mediaFileService;

	@Override
	@PostMapping(value = "/upload-start")
	public ResponseEntity<MediaFileUploadStartResDto> uploadStart(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestBody @Valid MediaFileUploadStartReqDto request
	) {
		return ResponseEntity.ok(mediaFileService.uploadStart(userPrincipal.getId(), request));
	}

	@Override
	@PostMapping(value = "/upload-chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> saveChunk(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestPart("chunk") MultipartFile chunk,
			@RequestParam("uploadId") UUID uploadId,
			@RequestParam("chunkNumber") int chunkNumber
	) {
		mediaFileService.saveChunk(userPrincipal.getId(), chunk, uploadId, chunkNumber);
		return ResponseEntity.accepted().build();
	}

	@Override
	@PostMapping(value = "/upload-complete")
	public ResponseEntity<MediaFileUploadResDto> completeUpload(
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
			@RequestBody @Valid MediaFileUploadCompleteReqDto request
	) {
		MediaFileUploadResDto response = mediaFileService.completeUpload(userPrincipal.getId(), request);
		return ResponseEntity.ok(response);
	}

}
