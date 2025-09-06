package dev.kyudong.back.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class FileIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser() {
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		return userRepository.save(newUser);
	}

	private static Stream<Arguments> fileProvider() {
		return Stream.of(
				Arguments.of("gif/sample.gif", "image/gif"),
				Arguments.of("images/sample1.jpg", "image/jpg"),
				Arguments.of("images/sample2.png", "image/png"),
				Arguments.of("pdf/sample.pdf", "application/pdf"),
				Arguments.of("txt/sample.txt", "text/plain"),
				Arguments.of("videos/sample.mp4", "video/mp4")
		);
	}

	@ParameterizedTest
	@DisplayName("파일 업로드 API")
	@MethodSource("fileProvider")
	void fileUplad_success(String fileName, String mimeType) throws Exception {
		// given
		User user = createTestUser();
		ClassPathResource resource = new ClassPathResource("test-files/" + fileName);
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				fileName.substring(fileName.lastIndexOf("/") + 1),
				mimeType,
				resource.getInputStream()
		);

		// when
		MvcResult result = mockMvc.perform(multipart("/api/v1/files/temp")
							.file(multipartFile)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user)))
						.andExpect(status().isCreated())
						.andExpect(header().exists("Location"))
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FileUploadResDto response = objectMapper.readValue(responseBody, FileUploadResDto.class);
		assertThat(response).isNotNull();

		Optional<File> storedFile = fileRepository.findByIdAndUploader(response.id(), user);
		assertThat(storedFile).isPresent();

		File file = storedFile.get();
		assertThat(file.getWebPath()).isEqualTo(response.webPath());
	}

}
