package dev.kyudong.back.file.service;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileStatus;
import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.properties.FileStorageProperties;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

public class FileServiceTests extends UnitTestBase {

	@Mock
	private FileRepository fileRepository;

	@Mock
	private FileStorageManager fileStorageManager;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@SuppressWarnings("unused")
	@Mock
	private FileStorageProperties fileStorageProperties;

	@InjectMocks
	private FileService fileService;

	@Mock
	private UserReaderService userReaderService;

	@Nested
	@DisplayName("파일 업로드")
	class UploadFile {

		private static final String UPLOAD_DIR_EXAMPLE = "/example/uploads";

		@ParameterizedTest
		@DisplayName("성공 : 파일은 초기에 PENDING 상태로 DB에 저장")
		@CsvSource({
				"gif/sample.gif,    image/gif",
				"images/sample1.jpg,  image/jpg",
				"images/sample2.png,  image/png",
				"pdf/sample.pdf,    application/pdf",
				"txt/sample.txt,    text/plain",
				"videos/sample.mp4, video/mp4"
		})
		void success(String fileName, String mimeType) throws Exception {
			// given
			ClassPathResource resource = new ClassPathResource("test-files/" + fileName);
			MockMultipartFile multipartFile = new MockMultipartFile(
					"file",
					fileName.substring(fileName.lastIndexOf("/") + 1),
					mimeType,
					resource.getInputStream()
			);

			User mockUser = createMockUser();
			given(userReaderService.getUserReference(mockUser.getId())).willReturn(mockUser);

			String expectedStorePath = UPLOAD_DIR_EXAMPLE + "/" + fileName;
			given(fileStorageManager.store(any(MultipartFile.class), anyString()))
					.willReturn(expectedStorePath);

			given(fileRepository.save(any(File.class))).willAnswer(invocation -> {
				File acFile = invocation.getArgument(0);
				ReflectionTestUtils.setField(acFile, "id", 1L);
				return acFile;
			});

			given(fileStorageProperties.getPublicWebPath()).willReturn("/media");

			// when
			FileUploadResDto response = fileService.storeTempFile(mockUser.getId(), multipartFile);

			// then
			assertThat(response).isNotNull();
			assertThat(response.webPath()).startsWith("/media");

			ArgumentCaptor<File> fileArgumentCaptor = ArgumentCaptor.forClass(File.class);
			then(fileRepository).should(times(1)).save(fileArgumentCaptor.capture());
			then(fileStorageManager).should(times(1)).store(any(MultipartFile.class), anyString());

			// 엔티티 검증
			File capturedFile = fileArgumentCaptor.getValue();
			assertThat(capturedFile.getUploader()).isEqualTo(mockUser);
			assertThat(capturedFile.getFilePath()).isEqualTo(expectedStorePath);
			assertThat(capturedFile.getStatus()).isEqualTo(FileStatus.PENDING);
		}

		@Test
		@DisplayName("실패 : 사용자 조회 실패")
		void fail_userNotFound() throws IOException {
			// given
			MockMultipartFile multipartFile = new MockMultipartFile(
					"file",
					"hello.jpeg",
					"image/jpeg",
					"HelloWorld".getBytes()
			);
			final Long nonExistsUserId = 999L;
			doThrow(new UserNotFoundException(nonExistsUserId)).when(userReaderService).getUserReference(nonExistsUserId);

			// when && then
			assertThatThrownBy(() -> fileService.storeTempFile(nonExistsUserId, multipartFile))
					.isInstanceOf(UserNotFoundException.class)
					.hasMessageContaining(String.valueOf(nonExistsUserId));

			then(fileStorageManager).should(never()).store(any(MultipartFile.class), anyString());
			then(fileRepository).should(never()).save(any(File.class));
		}

	}

}
