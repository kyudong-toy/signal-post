package dev.kyudong.back.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.jwt.JwtUtil;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
		final Long postId = savedPost.getId();

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
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
		PostCreateReqDto request = new PostCreateReqDto( "Test", "Hello Post!", new HashSet<>());

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/posts")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isCreated())
						.andExpect(header().exists("Location"))
						.andExpect(header().string("Location", CoreMatchers.containsString("/api/v1/posts/")))
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
		PostUpdateReqDto request = new PostUpdateReqDto("Test", "Hello Java!", new HashSet<>(), new HashSet<>());

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/posts/{postId}/update", savedPost.getId())
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
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
		PostStatusUpdateReqDto request = new PostStatusUpdateReqDto(PostStatus.DELETED);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}/status", savedPost.getId())
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
							.contentType(MediaType.APPLICATION_JSON.toString())
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.postId").value(savedPost.getId()))
					.andExpect(jsonPath("$.status").value(PostStatus.DELETED.name()))
					.andDo(print())
					.andReturn();
	}

}
