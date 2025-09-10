package dev.kyudong.back.post.tag;

import dev.kyudong.back.post.application.port.out.web.TagPersistencePort;
import dev.kyudong.back.post.application.service.web.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

	@InjectMocks
	private TagService tagService;

	@Mock
	private TagPersistencePort tagPersistencePort;

	@Test
	@DisplayName("태그 조회 - 성공")
	void findTagNamesByQuery_success() {
		// given
		String query = "스";

		given(tagPersistencePort.findByNameStartingWithOrderByPopularity(query))
				.willReturn(List.of("스프링", "스시", "스프"));

		// when
		List<String> tags = tagService.findTagNamesByQuery(query);

		// then
		assertThat(tags.size()).isEqualTo(3);
	}


}
