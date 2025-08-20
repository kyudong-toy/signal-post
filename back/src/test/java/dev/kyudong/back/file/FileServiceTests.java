package dev.kyudong.back.file;

import dev.kyudong.back.file.api.dto.res.FileUploadResDto;
import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileOwnerType;
import dev.kyudong.back.file.domain.FileStatus;
import dev.kyudong.back.file.exception.FileMetadataNotFoundException;
import dev.kyudong.back.file.manager.FileStorageManager;
import dev.kyudong.back.file.properties.FileStorageProperties;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.file.service.FileService;
import dev.kyudong.back.post.api.dto.event.PostCreatedEvent;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTests {

	@Mock
	private FileRepository fileRepository;

	@Mock
	private UserRepository userRepository;

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

	private static final String UPLOAD_DIR_EXAMPLE = "/example/uploads";

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
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
	@DisplayName("파일 업로드 - 성공 : 파일은 초기에 PENDING 상태로 DB에 저장")
	@MethodSource("fileProvider")
	void uploadFile_success(String fileName, String mimeType) throws Exception {
		// given
		ClassPathResource resource = new ClassPathResource("test-files/" + fileName);
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				fileName.substring(fileName.lastIndexOf("/") + 1),
				mimeType,
				resource.getInputStream()
		);

		User mockUser = makeMockUser();
		given(userRepository.existsById(mockUser.getId())).willReturn(true);
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

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
	@DisplayName("파일 업로드 실패 - 사용자 조회 실패")
	void uploadFile_fail_userNotFound() throws IOException {
		// given
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				"hello.jpeg",
				"image/jpeg",
				"HelloWorld".getBytes()
		);
		final Long nonExistsUserId = 999L;
		given(userRepository.existsById(nonExistsUserId)).willReturn(false);

		// when && then
		assertThatThrownBy(() -> fileService.storeTempFile(nonExistsUserId, multipartFile))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(String.valueOf(nonExistsUserId));

		then(userRepository).should(never()).getReferenceById(eq(nonExistsUserId));
		then(fileStorageManager).should(never()).store(any(MultipartFile.class), anyString());
		then(fileRepository).should(never()).save(any(File.class));
	}

	@Test
	@DisplayName("게시글 생성 후 파일 이벤트 - 성공")
	void handlePostCreateEvent_success() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.existsById(mockUser.getId())).willReturn(true);
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		File mockFile1 = File.builder()
				.uploader(mockUser)
				.build();
		ReflectionTestUtils.setField(mockFile1, "id", 1L);
		given(fileRepository.findByIdAndUploader(mockFile1.getId(), mockUser)).willReturn(Optional.of(mockFile1));
		File mockFile2 = File.builder()
				.uploader(mockUser)
				.build();
		ReflectionTestUtils.setField(mockFile2, "id", 2L);
		given(fileRepository.findByIdAndUploader(mockFile2.getId(), mockUser)).willReturn(Optional.of(mockFile2));

		PostCreatedEvent request = new PostCreatedEvent(1L, 1L, List.of(1L, 2L));

		// when
		fileService.handlePostCreatedEvent(request);

		// then
		assertThat(mockFile1.getStatus()).isEqualTo(FileStatus.ACTIVE);
		assertThat(mockFile1.getOwnerId()).isEqualTo(request.postId());
		assertThat(mockFile1.getFileOwnerType()).isEqualTo(FileOwnerType.POST);
		assertThat(mockFile2.getStatus()).isEqualTo(FileStatus.ACTIVE);
		assertThat(mockFile2.getOwnerId()).isEqualTo(request.postId());
		assertThat(mockFile2.getFileOwnerType()).isEqualTo(FileOwnerType.POST);
		then(fileRepository).should(times(2)).findByIdAndUploader(anyLong(), any(User.class));
	}

	@Test
	@DisplayName("게시글 생성 후 파일 이벤트 - 실패 : 파일 메타데이터가 일치하지 않음")
	void handlePostCreateEvent_fail_fileMetadataNotFound() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.existsById(mockUser.getId())).willReturn(true);
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		File mockFile1 = File.builder()
				.uploader(mockUser)
				.build();
		ReflectionTestUtils.setField(mockFile1, "id", 999L);

		PostCreatedEvent request = new PostCreatedEvent(1L, 1L, List.of(1L, 2L));

		// when && then
		assertThatThrownBy(() -> fileService.handlePostCreatedEvent(request))
				.isInstanceOf(FileMetadataNotFoundException.class)
				.hasMessageContaining(String.valueOf(1L));
	}

}
