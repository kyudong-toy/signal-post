package dev.kyudong.back.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.media.api.MediaFileController;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadCompleteReqDto;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.domain.MediaFileType;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaFileController.class)
@Import(SecurityConfig.class)
public class MediaFileControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private MediaFileService mediaFileService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("파일 업로드 시작 API - 성공")
	@WithMockCustomUser
	void uploadStartApi_success() throws Exception {
		// given
		final Long uploaderId = 1L;
		MediaFileUploadStartReqDto request = new MediaFileUploadStartReqDto("test.mp4", "video/mp4", 1000L, 10, MediaFileType.VIDEO);

		// when & then
		mockMvc.perform(post("/api/v1/files/upload-start")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
		then(mediaFileService).should().uploadStart(uploaderId, request);
	}

	@Test
	@DisplayName("파일 업로드 API - 실패")
	@WithMockCustomUser
	void uploadChunkApi_fail() throws Exception {
		// given
		final Long uploaderId = 1L;
		MockMultipartFile chunk = new MockMultipartFile(
				"chunk",
				"파일.jpeg",
				"images/jpeg",
				"HelloWorld".getBytes()
		);
		final UUID uploadId = UUID.randomUUID();
		final int chunkNumber = 1;

		// when & then
		mockMvc.perform(multipart("/api/v1/files/upload-chunk")
						.file(chunk)
						.param("uploadId", String.valueOf(uploadId))
						.param("chunkNumber", String.valueOf(chunkNumber)))
				.andExpect(status().isAccepted())
				.andDo(print());
		then(mediaFileService).should().saveChunk(uploaderId, chunk, uploadId, chunkNumber);
	}

	@Test
	@DisplayName("파일 업로드 완료 API - 실패")
	@WithMockCustomUser
	void completeUploadApi_fail() throws Exception {
		// given
		final Long uploaderId = 1L;
		MediaFileUploadCompleteReqDto request = new MediaFileUploadCompleteReqDto(UUID.randomUUID());

		// when & then
		mockMvc.perform(post("/api/v1/files/upload-complete")
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
		then(mediaFileService).should().completeUpload(uploaderId, request);
	}

}
