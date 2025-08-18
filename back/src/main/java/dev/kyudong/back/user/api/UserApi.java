package dev.kyudong.back.user.api;

import dev.kyudong.back.user.api.dto.req.UserCreateReqDto;
import dev.kyudong.back.user.api.dto.req.UserLoginReqDto;
import dev.kyudong.back.user.api.dto.req.UserStatusUpdateReqDto;
import dev.kyudong.back.user.api.dto.req.UserUpdateReqDto;
import dev.kyudong.back.user.api.dto.res.UserCreateResDto;
import dev.kyudong.back.user.api.dto.res.UserLoginResDto;
import dev.kyudong.back.user.api.dto.res.UserStatusUpdateResDto;
import dev.kyudong.back.user.api.dto.res.UserUpdateResDto;
import dev.kyudong.back.user.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * User API의 명세를 정의하는 인터페이스입니다.
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
@Tag(name = "User API", description = "사용자 관련 API 명세서")
public interface UserApi {

	@SuppressWarnings("unused")
	@Operation(summary = "사용자 생성 (회원가입)", description = "새로운 사용자를 생성합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "회원가입 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = UserCreateResDto.class),
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
			@ApiResponse(responseCode = "409", description = "이미 존재하는 사용자 이름",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Duplicate User",
										"status": 409,
										"detail": "testUser Already Exists",
										"instance": "/api/v1/users",
										"timestamp": "2025-08-16T04:58:39.816931700Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<UserCreateResDto> createUser(@RequestBody @Valid UserCreateReqDto request);

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
										"title": "User Not Found",
										"status": 404,
										"detail": "User: {testUser} Not Found",
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

	@SuppressWarnings("unused")
	@Operation(summary = "사용자 정보 수정", description = "사용자의 정보를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = UserUpdateResDto.class),
							examples = @ExampleObject(value =
									"""
									{
									  "id": 94,
									  "username": "testUser",
									  "token": "jwt토큰들어감"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자일 때",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: {1} Not Found",
										"instance": "/api/v1/users/me",
										"timestamp": "2025-08-16T05:06:40.722932200Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<UserUpdateResDto> updateUser(
			@RequestBody @Valid UserUpdateReqDto request,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal
	);

	@SuppressWarnings("unused")
	@Operation(summary = "사용자 상태 수정", description = "사용자의 상태(status)를 수정합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = UserStatusUpdateResDto.class),
							examples = @ExampleObject(value =
									"""
									{
										"id": 19,
										"status": "ACTIVE"
									}
									"""
							)
					)
			),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자일 때",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "User Not Found",
										"status": 404,
										"detail": "User: {1} Not Found",
										"instance": "/api/v1/users/1/status",
										"timestamp": "2025-08-16T05:09:12.094312100Z"
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
			@ApiResponse(responseCode = "400", description = "사용자 상태 정보 업데이트 실패시",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class),
							examples = @ExampleObject(value =
									"""
									{
										"type": "about:blank",
										"title": "Invalid Input Value",
										"status": 400,
										"detail": "UserStatus Cant not be update",
										"instance": "/api/v1/users/login",
										"timestamp": "2025-08-16T05:05:22.280467400Z"
									}
									"""
							)
					)
			),
	})
	ResponseEntity<UserStatusUpdateResDto> updateUserStatus(
			@RequestBody @Valid UserStatusUpdateReqDto request,
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal userPrincipal);

}
