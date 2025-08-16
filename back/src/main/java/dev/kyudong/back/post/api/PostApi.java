package dev.kyudong.back.post.api;

import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Post API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "Post API", description = "게시글 관련 API 명세서")
public interface PostApi {

	@SuppressWarnings("unused")
	@Operation(summary = "게시글 조회", description = "게시글 하나를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글 조회 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostDetailResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"id": 1,
										"userName": "testUser"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Post Not Found",
										"status": 404,
										"detail": "Post {999} Not Found",
										"instance": "/api/v1/post/999",
										"timestamp": "2025-08-16T16:20:22.367368100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<PostDetailResDto> findPostById(
			@Parameter(name = "postId", description = "조회할 게시글의 ID", required = true, example = "1")
			@PathVariable long postId
	);

	@SuppressWarnings("unused")
	@Operation(summary = "게시글 생성", description = "게시글을 생성합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "게시글 생성 성공",
					headers = @Header(
							name = "Location",
							description = "생성된 게시글 리소스의 URI",
							schema = @Schema(type = "string", format = "uri"),
							examples = @ExampleObject(value = "/api/v1/post/1")
					),
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostCreateReqDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"userId": 1,
										"subject": "Subject",
										"content": "This is Content"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "게시글 작성자 조회 실패로 글작성 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User {999} Not Found",
										"instance": "/api/v1/post",
										"timestamp": "2025-08-16T16:20:57.819701100Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<PostCreateResDto> createUser(@RequestBody PostCreateReqDto request);

	@SuppressWarnings("unused")
	@Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글 수정에 성공했습니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostUpdateReqDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"userId": 1,
										"subject": "New Subject",
										"content": "This is Chagne Content"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "게시글 조회 실패로 수정 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Post Not Found",
										"status": 404,
										"detail": "Post {999} Not Found",
										"instance": "/api/v1/post/999/status",
										"timestamp": "2025-08-16T16:21:54.535513500Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "401", description = "게시글 작성자의 수정 권한 불가로 수정 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Access Denied",
										"status": 401,
										"detail": "User {999} has no permission to update post 23",
										"instance": "/api/v1/post/23/update",
										"timestamp": "2025-08-16T16:24:35.784899900Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<PostUpdateResDto> updatePost(
			@Parameter(name = "postId", description = "수정할 게시글의 ID", required = true, example = "1")
			@PathVariable long postId,
			@RequestBody PostUpdateReqDto request
	);

	@SuppressWarnings("unused")
	@Operation(summary = "게시글 상태 수정", description = "게시글의 상태를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글의 상태 수정을 성공했습니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostStatusUpdateReqDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"userId": 1,
										"status": "New Subject",
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 게시글입니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Post Not Found",
										"status": 404,
										"detail": "Post {999} Not Found",
										"instance": "/api/v1/post/999/status",
										"timestamp": "2025-08-16T16:25:26.736754700Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "401", description = "게시글 작성자의 수정 권한 불가로 수정 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Access Denied",
										"status": 401,
										"detail": "User {999} has no permission to update post status 23",
										"instance": "/api/v1/post/23/status",
										"timestamp": "2025-08-16T16:26:08.968567300Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<PostStatusUpdateResDto> updatePostStatus(
			@Parameter(name = "postId", description = "상태를 수정할 게시글의 ID", required = true, example = "1")
			@PathVariable long postId,
			@RequestBody PostStatusUpdateReqDto request
	);

}
