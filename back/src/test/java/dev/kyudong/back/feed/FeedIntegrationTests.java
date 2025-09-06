package dev.kyudong.back.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.feed.api.dto.res.FeedDetailResDto;
import dev.kyudong.back.feed.domain.Feed;
import dev.kyudong.back.feed.repository.FeedRepository;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class FeedIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private FollowRepository followRepository;

	@Autowired
	private FeedRepository feedRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	private User createTestUser(String username) {
		User newUser = User.builder()
				.username(username)
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		return userRepository.save(newUser);
	}

	private Post createTestPost(User user) {
		Post newPost = Post.of("제목", "", Category.builder().build());
		user.addPost(newPost);
		return postRepository.save(newPost);
	}

	private void createTestFollow(User follower, User following) {
		Follow follow = Follow.builder()
				.follower(follower)
				.following(following)
				.build();
		followRepository.save(follow);
	}

	private void createTestFeed(User user, Post post) {
		Feed newFeed = Feed.builder()
				.user(user)
				.post(post)
				.build();
		feedRepository.save(newFeed);
	}

	@Test
	@DisplayName("피드 조회")
	void findFeeds_success() throws Exception {
		// given
		User follower = createTestUser("follower");
		User following = createTestUser("following");
		createTestFollow(follower, following);

		Post post = createTestPost(following);
		createTestFeed(follower, post);

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/feeds")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(follower))
							.param("size", "10"))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FeedDetailResDto response = objectMapper.readValue(responseBody, FeedDetailResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.content().get(0).postId()).isEqualTo(post.getId());
		assertThat(response.hasNext()).isFalse();
	}

}
