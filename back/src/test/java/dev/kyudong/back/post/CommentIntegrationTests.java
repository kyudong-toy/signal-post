package dev.kyudong.back.post;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.api.dto.req.*;
import dev.kyudong.back.post.api.dto.res.*;
import dev.kyudong.back.post.domain.Comment;
import dev.kyudong.back.post.domain.CommentStatus;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.repository.CommentRepository;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CommentIntegrationTests {

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
	private CommentRepository commentRepository;

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

	private Post createTestPost(User user) {
		Post newPost = Post.builder()
				.subject("Test")
				.content("Hello World!")
				.build();
		user.addPost(newPost);
		return postRepository.save(newPost);
	}

	@Test
	@DisplayName("댓글 조회 API")
	void findPostById() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment1 = Comment.builder()
				.content("댓글1")
				.user(user)
				.build();
		Comment comment2 = Comment.builder()
				.content("댓글2")
				.user(user)
				.build();
		post.addComments(List.of(comment1, comment2));
		commentRepository.save(comment1);
		commentRepository.save(comment2);

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/posts/{postId}/comments", post.getId()))
									.andExpect(status().isOk())
									.andDo(print())
									.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		List<CommentDetailResDto> response = objectMapper.readValue(responseBody, new TypeReference<>() {});
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.get(0).content()).isEqualTo("댓글1");
	}

	@Test
	@DisplayName("댓글 생성 API")
	void createComment() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		CommentCreateReqDto request = new CommentCreateReqDto("Hello Comment");

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/posts/{postId}/comments", post.getId())
										.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
										.contentType(MediaType.APPLICATION_JSON.toString())
										.content(objectMapper.writeValueAsString(request)))
									.andExpect(status().isCreated())
									.andDo(print())
									.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		CommentCreateResDto response = objectMapper.readValue(responseBody, CommentCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.postId()).isEqualTo(post.getId());
		assertThat(response.content()).isEqualTo("Hello Comment");
	}

	@Test
	@DisplayName("댓글 수정 API")
	void updateComment() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment = Comment.builder()
				.content("Comment Content")
				.user(user)
				.build();
		post.addComment(comment);
		commentRepository.save(comment);

		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}",post.getId(), comment.getId())
										.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
										.contentType(MediaType.APPLICATION_JSON.toString())
										.content(objectMapper.writeValueAsString(request)))
									.andExpect(status().isOk())
									.andDo(print())
									.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		CommentUpdateResDto response = objectMapper.readValue(responseBody, CommentUpdateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.userId()).isEqualTo(user.getId());
		assertThat(response.content()).isEqualTo("Hello Comment");
	}

	@Test
	@DisplayName("댓글 상태 수정 API")
	void updateCommentStatusApi_fail() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment = Comment.builder()
				.content("Comment Content")
				.user(user)
				.build();
		post.addComment(comment);
		Comment savedComment = commentRepository.save(comment);

		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}/status", post.getId(), savedComment.getId())
											.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
											.contentType(MediaType.APPLICATION_JSON.toString())
											.content(objectMapper.writeValueAsString(request)))
									.andExpect(status().isOk())
									.andDo(print())
									.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		CommentStatusUpdateResDto response = objectMapper.readValue(responseBody, CommentStatusUpdateResDto.class);
		assertThat(response.commentId()).isEqualTo(savedComment.getId());
		assertThat(response.status()).isEqualTo(CommentStatus.DELETED);
	}

}
