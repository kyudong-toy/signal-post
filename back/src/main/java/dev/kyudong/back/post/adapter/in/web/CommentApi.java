package dev.kyudong.back.post.adapter.in.web;

import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Comment API의 명세를 정의하는 인터페이스입니다.
 * <p>
 * 이 인터페이스의 메소드들은 실제 코드에서 직접 호출되지 않지만,
 * Spring MVC 프레임워크에 의해 런타임 시 동적으로 구현체(Controller)와 연결되어 사용됩니다.
 * <p>
 * 따라서 IDE에서 '사용되지 않음(unused)'으로 잘못 분석할 수 있으므로,
 * {@code @SuppressWarnings("unused")}를 사용하여 관련 경고를 억제합니다.
 *
 * @see org.springframework.web.bind.annotation.RestController
 * @see SuppressWarnings
 */
@Tag(name = "Comment API", description = "댓글 관련 API 명세서")
public interface CommentApi {

	@SuppressWarnings("unused")
	@Operation(summary = "조회한 게시글의 댓글 목록 조회", description = "조회한 게시글의 댓글 목록을 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "댓글 조회 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostDetailResDto.class),
							examples = @ExampleObject(value =
									"""
									[
										{
											"postId": 999,
											"userId": 999,
											"commentId": 999,
											"content": "댓글 본문",
											"status": "NORMAL",
											"createdAt": "2025-08-18T01:13:14.621947",
											"modifiedAt": "2025-08-18T01:13:14.621947"
										}
									]
									"""
							)
					)
			),
	})
	ResponseEntity<List<CommentDetailResDto>> findCommentsByPostId(
			@Parameter(name = "postId", description = "조회할 댓글의 게시글 ID", required = true, example = "1")
			@PathVariable @Positive Long postId
	);

	@SuppressWarnings("unused")
	@Operation(summary = "댓글 생성", description = "댓글을 생성합니다.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "댓글 생성 성공",
					headers = @Header(
							name = "Location",
							description = "생성된 댓글 리소스의 URI",
							schema = @Schema(type = "string", format = "uri"),
							examples = @ExampleObject(value = "/api/v1/comment/1")
					),
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = CommentCreateResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"userId": 999,
										"postId": 999,
										"commentId": 999,
										"content": "댓글 테스트",
										"status": "NORMAL",
										"createdAt": "2025-08-18T02:36:09.6790352",
										"modifiedAt": "2025-08-18T02:36:09.6790352"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "게시글 조회 실패로 댓글 작성 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User {999} Not Found",
										"instance": "/api/v1/posts/999/comments",
										"timestamp": "2025-08-16T16:20:57.819701100Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "작성자 조회 실패로 댓글 작성 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: {999} Not Found",
										"instance": "/api/v1/posts/999/comments",
										"timestamp": "2025-08-18T02:37:47.421383400Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<CommentCreateResDto> createComment(
			@Parameter(name = "postId", description = "댓글의 작성할 게시글 ID", required = true, example = "1")
			@PathVariable @Positive Long postId,
			@RequestBody @Valid CommentCreateReqDto request,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal);

	@SuppressWarnings("unused")
	@Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "댓글 수정에 성공했습니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = CommentUpdateResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"userId": 19,
										"postId": 23,
										"commentId": 2,
										"content": "수정!",
										"status": "NORMAL",
										"createdAt": "2025-08-18T01:13:14.621947",
										"modifiedAt": "2025-08-18T01:13:14.621947"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "게시글 조회 실패로 댓글 수정 실패",
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
			@ApiResponse(responseCode = "404", description = "댓글 조회 실패로 댓글 수정 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Comment Not Found",
										"status": 404,
										"detail": "Comment {999} Not Found",
										"instance": "/api/v1/posts/23/comments/999",
										"timestamp": "2025-08-16T16:21:54.535513500Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "401", description = "댓글 수정을 요청한 사용자의 권한 불가로 수정 실패",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Access Denied",
										"status": 401,
										"detail": "User {999} has no permission to update Comment 2",
										"instance": "/api/v1/posts/23/comments/2",
										"timestamp": "2025-08-18T02:39:20.153752Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<CommentUpdateResDto> updateComment(
			@Parameter(name = "postId", description = "수정할 게시글의 ID", required = true, example = "1")
			@PathVariable @Positive Long postId,
			@Parameter(name = "commentId", description = "수정할 댓글의 ID", required = true, example = "1")
			@PathVariable @Positive Long commentId,
			@RequestBody @Valid CommentUpdateReqDto request,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

	@SuppressWarnings("unused")
	@Operation(summary = "댓글 상태 수정", description = "댓글의 상태를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글의 상태 수정을 성공했습니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostStatusUpdateReqDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"postId": 23,
										"commentId": 2,
										"status": "NORMAL"
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
										"instance": "/api/v1/posts/999/comments/2/status",
										"timestamp": "2025-08-16T16:25:26.736754700Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 댓글입니다.",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Comment Not Found",
										"status": 404,
										"detail": "Comment {999} Not Found",
										"instance": "/api/v1/posts/23/comments/999/status",
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
										"detail": "User {999} has no permission to update Comment status 2",
										"instance": "/api/v1/posts/23/comments/2/status",
										"timestamp": "2025-08-16T16:26:08.968567300Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<CommentStatusUpdateResDto> updateCommentStatus(
			@Parameter(name = "postId", description = "댓글 상태를 수정할 게시글의 ID", required = true, example = "1")
			@PathVariable @Positive Long postId,
			@Parameter(name = "commentId", description = "수정할 댓글의 ID", required = true, example = "1")
			@PathVariable @Positive Long commentId,
			@RequestBody @Valid CommentStatusUpdateReqDto request,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

}
