package dev.kyudong.back.post.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.IntegrationTestBase;
import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.adapter.out.persistence.repository.TagRepository;
import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.entity.*;
import dev.kyudong.back.post.adapter.out.persistence.repository.CategoryRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.CategoryTranslationRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PostIntegrationTests extends IntegrationTestBase {

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
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryTranslationRepository categoryTranslationRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		Category category1 = Category.builder()
				.categoryCode("test_1")
				.build();
		categoryRepository.save(category1);

		CategoryTranslation translation1_ko = CategoryTranslation.builder()
				.category(category1)
				.languageCode("ko-KR")
				.name("일상(ko)")
				.build();
		categoryTranslationRepository.save(translation1_ko);

		Category category2 = Category.builder()
				.categoryCode("test_2")
				.build();
		categoryRepository.save(category2);

		CategoryTranslation translation2_ko = CategoryTranslation.builder()
				.category(category2)
				.languageCode("ko-KR")
				.name("IT(ko)")
				.build();
		categoryTranslationRepository.save(translation2_ko);
	}

	private User createTestUser() {
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword(passwordEncoder.encode("password"))
				.build();
		return userRepository.save(newUser);
	}

	private static String createMockTiptapContent() throws JsonProcessingException {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"content", List.of(textNode)
		);

		Map<String, Object> map = Map.of(
				"type", "doc",
				"content", List.of(paragraphNode)
		);

		return new ObjectMapper().writeValueAsString(map);
	}

	@Test
	@DisplayName("사용자 게시글 조회 API")
	void findPostById_withUser() throws Exception {
		// given
		Category category = categoryRepository.findByCategoryCode("test_1").orElseThrow();
		User user = createTestUser();
		Post newPost = Post.create(
				"제목",
				createMockTiptapContent(),
				category
		);
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);
		final Long postId = savedPost.getId();
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
						.cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value(newPost.getSubject()))
				.andDo(print());
	}

	@Test
	@DisplayName("게스트 게시글 조회 API")
	void findPostById_withGuest() throws Exception {
		// given
		Category category = categoryRepository.findByCategoryCode("test_1").orElseThrow();
		User user = createTestUser();
		Post newPost = Post.create(
				"제목",
				createMockTiptapContent(),
				category
		);
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);
		final Long postId = savedPost.getId();
		final String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);

		// when & then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
						.cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postId").value(postId))
				.andExpect(jsonPath("$.subject").value(newPost.getSubject()))
				.andDo(print());
	}

	@Test
	@DisplayName("게시글 생성 (태그 미포함) API")
	void createPost() throws Exception {
		// given
		User user = createTestUser();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				createMockTiptapContent(),
				"test_1",
				new HashSet<>(),
				new HashSet<>()
		);

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

		Optional<Post> optionalPost = postRepository.findById(response.postId());
		assertThat(optionalPost).isPresent();

		Post post = optionalPost.get();
		assertThat(post.getUser().getId()).isEqualTo(user.getId());
		assertThat(post.getId()).isEqualTo(response.postId());
	}

	@Test
	@DisplayName("게시글 생성 (태그 포함) API")
	void createPost_withTags() throws Exception {
		// given
		User user = createTestUser();
		PostCreateReqDto request = new PostCreateReqDto(
				"subject",
				createMockTiptapContent(),
				"test_1",
				new HashSet<>(),
				Set.of("새로운태그", "기존태그")
		);

		Tag tag = Tag.of("기존태그");
		tagRepository.save(tag);

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
		Category category = categoryRepository.findByCategoryCode("test_1").orElseThrow();
		User user = createTestUser();
		Post newPost = Post.create(
				"제목",
				createMockTiptapContent(),
				category
		);
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);

		PostUpdateReqDto request = new PostUpdateReqDto(
				"Test",
				createMockTiptapContent(),
				"test_1",
				new HashSet<>(),
				new HashSet<>()
		);

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
		Category category = categoryRepository.findByCategoryCode("test_1").orElseThrow();
		User user = createTestUser();
		Post newPost = Post.create(
				"제목",
				createMockTiptapContent(),
				category
		);
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
