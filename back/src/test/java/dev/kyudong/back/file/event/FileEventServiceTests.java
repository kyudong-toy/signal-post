package dev.kyudong.back.file.event;

import dev.kyudong.back.file.domain.FileOwnerType;
import dev.kyudong.back.file.properties.FileStorageProperties;
import dev.kyudong.back.file.repository.FileRepository;
import dev.kyudong.back.file.service.FileEventService;
import dev.kyudong.back.post.domain.dto.event.PostCreateFileEvent;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class FileEventServiceTests extends UnitTestBase {

	@Mock
	private FileRepository fileRepository;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@SuppressWarnings("unused")
	@Mock
	private FileStorageProperties fileStorageProperties;

	@InjectMocks
	private FileEventService fileEventService;

	@Test
	@DisplayName("게시글 생성 후 파일 이벤트 - 성공")
	void handlePostCreateEvent_success() {
		// given
		Long postId = 1L;
		Set<Long> fileIds = Set.of(1L, 2L);
		PostCreateFileEvent request = new PostCreateFileEvent(postId, fileIds);

		// when
		fileEventService.handlePostCreateEvent(request);

		// then
		then(fileRepository).should().confirmFileByIds(eq(fileIds), eq(postId), eq(FileOwnerType.POST));
	}


}
