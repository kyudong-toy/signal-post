package dev.kyudong.back.media.event;

import dev.kyudong.back.media.domain.MediaFileOwnerType;
import dev.kyudong.back.media.properties.MediaFileStorageProperties;
import dev.kyudong.back.media.repository.MediaFileRepository;
import dev.kyudong.back.media.service.MediaFileEventService;
import dev.kyudong.back.post.domain.dto.event.PostCreateFileEvent;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class MediaMediaFileEventServiceTests extends UnitTestBase {

	@Mock
	private MediaFileRepository mediaFileRepository;

	@SuppressWarnings("unused")
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@SuppressWarnings("unused")
	@Mock
	private MediaFileStorageProperties mediaFileStorageProperties;

	@InjectMocks
	private MediaFileEventService mediaFileEventService;

	@Test
	@DisplayName("게시글 생성 후 파일 이벤트 - 성공")
	void handlePostCreateEvent_success() {
		// given
		Long postId = 1L;
		Set<Long> fileIds = Set.of(1L, 2L);
		PostCreateFileEvent request = new PostCreateFileEvent(postId, fileIds);

		// when
		mediaFileEventService.handlePostCreateEvent(request);

		// then
		then(mediaFileRepository).should().confirmFileByIds(eq(fileIds), eq(postId), eq(MediaFileOwnerType.POST));
	}


}
