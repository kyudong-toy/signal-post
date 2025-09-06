package dev.kyudong.back.feed;

import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.feed.api.FeedController;
import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.service.FeedService;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedController.class)
@Import(SecurityConfig.class)
public class FeedControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private FeedService feedService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("피드 조회 - 성공")
	@WithMockCustomUser(id = 999L)
	void findFeedsApi_success() throws Exception {
		// given
		final Long userId = 999L;
		int size = 10;
		FeedDetailResDto response = new FeedDetailResDto(100L, true, List.of(new PostDetailResDto(1L, 1L, "Test", "Hello World!", PostStatus.NORMAL, LocalDateTime.now(), LocalDateTime.now())));
		given(feedService.findFeeds(userId, null, size))
				.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/feeds")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastFeedId").value(100L))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andDo(print());
		then(feedService).should(times(1)).findFeeds(eq(userId), eq(null), eq(size));
	}

	@Test
	@DisplayName("피드 조회 - 성공: 피드 아이디로 조회")
	@WithMockCustomUser(id = 999L)
	void findFeedsApi_success_withLastFeedId() throws Exception {
		// given
		final Long userId = 999L;
		final Long lastFeedId = 150L;
		int size = 10;
		FeedDetailResDto response = new FeedDetailResDto(lastFeedId, true, List.of(new PostDetailResDto(lastFeedId, 1L, "Test", "Hello World!", PostStatus.NORMAL, LocalDateTime.now(), LocalDateTime.now())));
		given(feedService.findFeeds(userId, lastFeedId, size)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/feeds")
						.param("size", "10")
						.param("lastFeedId", "150"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastFeedId").value(lastFeedId))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andDo(print());
		then(feedService).should(times(1)).findFeeds(eq(userId), eq(lastFeedId), eq(size));
	}

}
