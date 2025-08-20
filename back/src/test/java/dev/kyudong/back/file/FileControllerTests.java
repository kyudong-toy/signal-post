package dev.kyudong.back.file;

import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.file.api.FileController;
import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.exception.InvalidFileException;
import dev.kyudong.back.file.service.FileService;
import dev.kyudong.back.security.WithMockCustomUser;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@Import(SecurityConfig.class)
public class FileControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private FileService fileService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("파일 업로드 API - 성공")
	@WithMockCustomUser(id = 999L)
	void fileUpladApi_success() throws Exception {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				"hello.jpeg",
				"images/jpeg",
				"HelloWorld".getBytes()
		);

		final Long uploaderId = 999L;
		FileUploadResDto response = new FileUploadResDto(uploaderId ,"/test");
		given(fileService.storeTempFile(uploaderId, multipartFile))
				.willReturn(response);

		// when & then
		mockMvc.perform(multipart("/api/v1/files/temp")
						.file(multipartFile))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").value(uploaderId))
				.andExpect(jsonPath("$.webPath").value("/test"))
				.andDo(print());
		then(fileService).should(times(1)).storeTempFile(eq(uploaderId), any(MultipartFile.class));
	}

	@Test
	@DisplayName("게시글 조회 API - 실패")
	@WithMockCustomUser(id = 999L)
	void findPostByIdApi_fail() throws Exception {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				null,
				"images/jpeg",
				"HelloWorld".getBytes()
		);

		final Long uploaderId = 999L;
		given(fileService.storeTempFile(uploaderId, multipartFile))
				.willThrow(new InvalidFileException("파일 정보가 올바르지 않습니다."));

		// when & then
		mockMvc.perform(multipart("/api/v1/files/temp")
						.file(multipartFile))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid Request File"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("파일 정보가 올바르지 않습니다."))
				.andDo(print());
		then(userRepository).should(never()).existsById(eq(uploaderId));
	}

}
