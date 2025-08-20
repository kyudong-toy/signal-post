package dev.kyudong.back.file.api;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * File API의 명세를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스의 메소드들은 실제 코드에서 직접 호출되지 않지만,
 * Spring MVC 프레임워크에 의해 런타임 시 동적으로 구현체(Controller)와 연결되어 사용됩니다.
 * <p>
 * 따라서 IDE에서 '사용되지 않음(unused)'으로 잘못 분석할 수 있으므로,
 * {@code @SuppressWarnings("unused")}를 사용하여 관련 경고를 억제합니다.
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see java.lang.SuppressWarnings
 */
@Tag(name = "FILE API", description = "파일 관련 API 명세서")
public interface FileApi {

	@SuppressWarnings("unused")
	@Operation(summary = "파일 업로드",
			description = """
				파일을 대기 상태로 저장하며 파일을 첨부한
				원본(게시글, 댓글 등)이 저장될 경우 대기 상태에서 활성화 상태로 변경됩니다.
			""")
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "파일 업로드 성공",
					headers = @Header(
							name = "Location",
							description = "생성된 파일의 리소스의 URI",
							schema = @Schema(type = "string", format = "uri"),
							examples = @ExampleObject(value = "/media/1d3c0cd1-e01f-4b6b-a731-581214715ca2.test.jpeg")
					),
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = FileUploadResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"id": 1,
										"webPath": "testUser"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "401", description = "파일 요청 데이터가 누락되었습니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Invalid Request File",
										"status": 401,
										"detail": "Invalid Request File",
										"instance": "/api/v1/files/temp",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자입니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User {999} Not Found",
										"instance": "/api/v1/files/temp",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<FileUploadResDto> storeTempFile(
			@RequestParam("file") MultipartFile file,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	) throws IOException;

}

