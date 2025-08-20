package dev.kyudong.back.file.api;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.service.FileService;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController implements FileApi {

	private final FileService fileService;

	@PostMapping(value = "/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileUploadResDto> storeTempFile(
			@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal CustomUserPrincipal userPrincipal
	) throws IOException {
		FileUploadResDto fileUploadResDto = fileService.storeTempFile(userPrincipal.getId(), file);
		URI fileUri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path(fileUploadResDto.webPath())
				.build()
				.toUri();
		return ResponseEntity.created(fileUri).body(fileUploadResDto);
	}

}
