package dev.kyudong.back.media.service;

import dev.kyudong.back.media.api.dto.MediaFileInfo;
import dev.kyudong.back.media.api.dto.MediaFileTransaction;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadCompleteReqDto;
import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadResDto;
import dev.kyudong.back.media.api.dto.res.MediaFileUploadStartResDto;
import dev.kyudong.back.media.domain.MediaFile;
import dev.kyudong.back.media.domain.MediaFileType;
import dev.kyudong.back.media.manager.MediaFileStorageManager;
import dev.kyudong.back.media.repository.MediaFileRepository;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

public class MediaMediaFileServiceTests extends UnitTestBase {

	@Mock
	private MediaFileRepository mediaFileRepository;

	@Mock
	private MediaFileStorageManager mediaFileStorageManager;

	@InjectMocks
	private MediaFileService mediaFileService;

	@Mock
	private UserReaderService userReaderService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RBucket<MediaFileTransaction> mockBucket;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Nested
	@DisplayName("파일 업로드 : 총 3단계에 거쳐 진행됩니다")
	class UploadMediaFile {

		private final Long uploaderId = 1L;
		private final UUID uploadId = UUID.randomUUID();

		@BeforeEach
		void setUp() {
			doReturn(mockBucket).when(redissonClient).getBucket(anyString());
		}

		@Test
		@DisplayName("1단계 성공 : 파일 업로드 시작")
		void uploadStart_success() {
			// given
			MediaFileUploadStartReqDto request = new MediaFileUploadStartReqDto("test.mp4", "video/mp4", 1000L, 10, MediaFileType.VIDEO);

			// when
			MediaFileUploadStartResDto response = mediaFileService.uploadStart(uploaderId, request);

			// then
			assertThat(response.uploadId()).isNotNull();

			ArgumentCaptor<MediaFileTransaction> transactionCaptor = ArgumentCaptor.forClass(MediaFileTransaction.class);
			ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

			then(mockBucket).should(times(1)).set(transactionCaptor.capture(), durationCaptor.capture());

			MediaFileTransaction capturedTransaction = transactionCaptor.getValue();
			assertThat(capturedTransaction.getUploaderId()).isEqualTo(uploaderId);
			assertThat(capturedTransaction.getFileName()).isEqualTo(request.fileName());
			assertThat(capturedTransaction.getTotalChunkCount()).isEqualTo(request.totalChunkCount());
			assertThat(capturedTransaction.getReceivedChunks()).isNotNull().isEmpty();

			Duration capturedDuration = durationCaptor.getValue();
			assertThat(capturedDuration.toHours()).isEqualTo(1);
		}

		@Test
		@DisplayName("2단계 성공 : 새로운 청크가 도착하면 파일을 저장하고 Redis 트랜잭션을 갱신")
		void saveChunk_success() {
			// given
			int newChunkNumber = 1;
			MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk_5", "application/octet-stream", "data".getBytes());
			MediaFileUploadStartReqDto request = new MediaFileUploadStartReqDto("test.mp4", "video/mp4", 1000L, 10, MediaFileType.VIDEO);
			MediaFileTransaction transaction = MediaFileTransaction.of(uploaderId, request);

			given(mockBucket.get()).willReturn(transaction);

			// when
			mediaFileService.saveChunk(uploaderId, chunkFile, uploadId, newChunkNumber);

			// then
			then(mediaFileStorageManager).should(times(1)).store(chunkFile, uploadId, newChunkNumber);

			ArgumentCaptor<MediaFileTransaction> captor = ArgumentCaptor.forClass(MediaFileTransaction.class);
			then(mockBucket).should(times(1)).set(captor.capture(), any(Duration.class));

			assertThat(captor.getValue().getReceivedChunks()).contains(newChunkNumber);
		}

		@Test
		@DisplayName("3단계 성공 : 청크 전송이 끝나면 하나의 파일로 합치고 타입에 따라 추가 작업을 진행한다")
		void completeUpload_success() {
			// given
			MediaFileUploadStartReqDto data = new MediaFileUploadStartReqDto("test.mp4", "video/mp4", 1000L, 10, MediaFileType.VIDEO);
			int totalChunks = 10;
			MediaFileTransaction completeTransaction = MediaFileTransaction.of(uploaderId, data);
			for (int i = 0; i < totalChunks; i++) {
				completeTransaction.addChunk(i);
			}
			assertThat(completeTransaction.isComplete()).isTrue();
			given(mockBucket.get()).willReturn(completeTransaction);

			User mockUser = createMockUser();
			given(userReaderService.getUserReference(uploaderId)).willReturn(mockUser);

			given(mediaFileRepository.save(any(MediaFile.class))).willAnswer(invocation -> {
				MediaFile file = invocation.getArgument(0);
				ReflectionTestUtils.setField(file, "id", 101L);
				return file;
			});

			// when
			MediaFileUploadCompleteReqDto request = new MediaFileUploadCompleteReqDto(uploadId);
			MediaFileUploadResDto response = mediaFileService.completeUpload(uploaderId, request);

			// then
			assertThat(response.id()).isEqualTo(101L);

			ArgumentCaptor<MediaFile> mediaFileCaptor = ArgumentCaptor.forClass(MediaFile.class);
			then(mediaFileRepository).should().save(mediaFileCaptor.capture());

			MediaFile savedFile = mediaFileCaptor.getValue();
			assertThat(savedFile.getOriginalFileName()).isEqualTo("test.mp4");
			assertThat(savedFile.getUploader()).isEqualTo(mockUser);

			ArgumentCaptor<MediaFileInfo> messageCaptor = ArgumentCaptor.forClass(MediaFileInfo.class);
			then(rabbitTemplate).should().convertAndSend(
					eq("media.exchange"),
					eq("media.video.process.queue"),
					messageCaptor.capture()
			);

			MediaFileInfo fileInfo = messageCaptor.getValue();
			assertThat(fileInfo.fileId()).isEqualTo(101L);
			assertThat(fileInfo.uploadId()).isEqualTo(uploadId);

			then(mockBucket).should().delete();
		}

	}

}
