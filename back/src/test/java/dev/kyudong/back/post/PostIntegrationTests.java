package dev.kyudong.back.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.domain.PostStatus;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class PostIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	private User createTestUser() {
		return userRepository.save(new User("testUser", "password1234"));
	}

	@Test
	@DisplayName("게시글 조회 API")
	void findPostById() throws Exception {
		// given
		User user = createTestUser();
		Post newPost = Post.builder()
				.subject("Test")
				.content("Hello World!")
				.build();
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);
		long postId = savedPost.getId();

		// when & then
		mockMvc.perform(get("/api/v1/post/{postId}", postId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value("Test"))
				.andExpect(jsonPath("$.content").value("Hello World!"))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 API")
	void createPost() throws Exception {
		// given
		User user = createTestUser();
		PostCreateReqDto request = new PostCreateReqDto(user.getId(), "Test", "Hello Post!");

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/post")
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isCreated())
						.andExpect(header().exists("Location"))
						.andExpect(header().string("Location", CoreMatchers.containsString("/api/v1/post/")))
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		PostCreateResDto response = objectMapper.readValue(responseBody, PostCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.subject()).isEqualTo(request.subject());
		assertThat(response.content()).isEqualTo(request.content());

		Optional<Post> optionalPost = postRepository.findById(response.postId());
		assertThat(optionalPost).isPresent();

		Post post = optionalPost.get();
		assertThat(post.getUser().getId()).isEqualTo(user.getId());
		assertThat(post.getId()).isEqualTo(response.postId());
	}

	@Test
	@DisplayName("게시글 수정 API")
	void updatePost() throws Exception {
		// given
		User user = createTestUser();
		Post post = Post.builder()
				.subject("Test")
				.content("Hello World!")
				.build();
		user.addPost(post);
		Post savedPost = postRepository.save(post);
		PostUpdateReqDto request = new PostUpdateReqDto(user.getId(), "Test", "Hello Java!");

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/post/{postId}/update", savedPost.getId())
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		PostUpdateResDto response = objectMapper.readValue(responseBody, PostUpdateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.subject()).isEqualTo(request.subject());
		assertThat(response.content()).isNotEqualTo("Hello World!");
	}

	@Test
	@DisplayName("게시글 상태 수정 API")
	void updatePostStatus() throws Exception {
		// given
		User user = createTestUser();
		Post newPost = Post.builder()
				.subject("Test")
				.content("Hello World!!")
				.build();
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(user.getId(), PostStatus.DELETED);

		// when & then
		mockMvc.perform(patch("/api/v1/post/{postId}/status", savedPost.getId())
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.postId").value(savedPost.getId()))
					.andExpect(jsonPath("$.status").value(PostStatus.DELETED.name()))
					.andDo(print())
					.andReturn();
	}

}
