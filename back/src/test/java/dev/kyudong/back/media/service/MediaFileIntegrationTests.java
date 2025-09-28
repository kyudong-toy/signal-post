package dev.kyudong.back.media.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadCompleteReqDto;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadStartResDto;
import dev.kyudong.back.media.domain.MediaFileStatus;
import dev.kyudong.back.media.domain.MediaFileType;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadResDto;
import dev.kyudong.back.media.domain.MediaFile;
import dev.kyudong.back.media.repository.MediaFileRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MediaFileIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MediaFileRepository mediaFileRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser() {
		User newUser = User.create("tester", "rawPassword", "encodedPassword");
		return userRepository.save(newUser);
	}

	@ParameterizedTest
	@CsvSource({
			"images/sample1.jpg,  image/jpg",
			"images/sample2.png,  image/png",
			"videos/sample.mp4, video/mp4"
	})
	@DisplayName("파일 업로드 테스트: 업로드 완료 시, Worker가 비동기 처리 후 DB 상태를 PENDING로 변경한다")
	void fullFileUploadAndProcess_Success(String fileName, String mimeType) throws Exception {
		// given
		User user = createTestUser();
		String accessToken = "Bearer " + jwtUtil.createAccessToken(user);
		final long chunkSize = 10 * 1024 * 1024;

		ClassPathResource resource = new ClassPathResource("test-files/" + fileName);
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				fileName.substring(fileName.lastIndexOf("/") + 1),
				mimeType,
				resource.getInputStream()
		);
		byte[] allBytes = multipartFile.getBytes();

		final int totalChunks = (int) Math.ceil((double) multipartFile.getSize() / chunkSize);
		MediaFileType type = mimeType.split("/")[0].equals("video") ? MediaFileType.VIDEO : MediaFileType.IMAGE;

		// when
		// 1. 업로드 준비
		MediaFileUploadStartReqDto startRequest = new MediaFileUploadStartReqDto(
				multipartFile.getOriginalFilename(), multipartFile.getContentType(),
				multipartFile.getSize(), totalChunks, type
		);

		MvcResult startResult = mockMvc.perform(post("/api/v1/files/upload-start")
						.header(HttpHeaders.AUTHORIZATION, accessToken)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(startRequest)))
          		.andExpect(status().isOk())
				.andReturn();
		String uploadId = objectMapper.readValue(startResult.getResponse().getContentAsString(), MediaFileUploadStartResDto.class).uploadId();

		// 2. 모든 청크 업로드
		for (int i = 0; i < totalChunks; i++) {
			int start = i * (int) chunkSize;
			int end = Math.min(allBytes.length, start + (int) chunkSize);

			byte[] chunkBytes = Arrays.copyOfRange(allBytes, start, end);
			MockMultipartFile chunk = new MockMultipartFile(
					"chunk",
					"chunk_" + i,
					"application/octet-stream",
					chunkBytes
			);

			mockMvc.perform(multipart("/api/v1/files/upload-chunk")
							.file(chunk)
							.param("uploadId", String.valueOf(uploadId))
							.param("chunkNumber", String.valueOf(i))
							.header(HttpHeaders.AUTHORIZATION, accessToken))
					.andExpect(status().isAccepted());
		}

		// 3. 업로드 완료 요청 (이 요청으로 RabbitMQ에 메시지가 발행됨)
		MediaFileUploadCompleteReqDto request = new MediaFileUploadCompleteReqDto(UUID.fromString(uploadId));
		MvcResult completeResult = mockMvc.perform(post("/api/v1/files/upload-complete")
						.header(HttpHeaders.AUTHORIZATION, accessToken)
						.contentType(MediaType.APPLICATION_JSON.toString())
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andReturn();
		MediaFileUploadResDto responseDto = objectMapper.readValue(completeResult.getResponse().getContentAsString(), MediaFileUploadResDto.class);
		Long id = responseDto.id();

		// then
		Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			MediaFile mediaFile = mediaFileRepository.findById(id)
					.orElseThrow(() -> new AssertionError("MediaFile이 DB에 없습니다."));

			assertThat(mediaFile.getStatus()).isEqualTo(MediaFileStatus.PENDING);
		});
	}

}
