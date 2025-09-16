package dev.kyudong.back.user.api;

import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * USER Auth API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "USER Auth API", description = "사용자 인증 관련 API 명세서")
public interface UserAuthApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자 로그인", description = "새로운 사용자를 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = UserLoginResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"id": 1,
										"username": "testUser",
										"status": "ACTIVE"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "userName과 일치하는 사용자 찾지 못할 때",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "USER Not Found",
										"status": 404,
										"detail": "USER: {testUser} Not Found",
										"instance": "/api/v1/users/login",
										"timestamp": "2025-08-16T05:03:26.747698400Z"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "400", description = "비밀번호를 틀렸을 경우",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Invalid Input Value",
										"status": 400,
										"detail": "Password not Equals",
										"instance": "/api/v1/users/login",
										"timestamp": "2025-08-16T05:05:22.280467400Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<UserLoginResDto> loginUser(@RequestBody @Valid UserLoginReqDto request);

}
