package dev.kyudong.back.post.adapter.in.web;

import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Tag API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "Tag API", description = "게시글 태그 관련 API 명세서")
public interface TagApi {

	@SuppressWarnings("unused")
	@Operation(summary = "태그 조회", description = "입력한 글자로 태그를 조회합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "게시글 조회 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = PostDetailResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"id": 1,
										"username": "testUser"
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
	ResponseEntity<List<String>> findTagNamesByQuery(
			@Parameter(name = "query", description = "조회활 태그 이름", required = true)
			@RequestParam String query
	);

}
