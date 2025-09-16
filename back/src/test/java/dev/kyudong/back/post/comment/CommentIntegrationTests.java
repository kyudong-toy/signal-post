package dev.kyudong.back.post.comment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentStatus;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.repository.CommentRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CommentIntegrationTests extends IntegrationTestBase {

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

	private Post createTestPost(User user) throws JsonProcessingException {
		Post newPost = Post.create("제목", createMockTiptapContent());
		user.addPost(newPost);
		return postRepository.save(newPost);
	}

	private String createMockTiptapContent() throws JsonProcessingException {
		Map<String, Object> textNode = Map.of(
				"type", "text",
				"text", "테스트입니다"
		);

		Map<String, Object> paragraphNode = Map.of(
				"type", "paragraph",
				"contents", List.of(textNode)
		);

		Map<String, Object> map = Map.of(
				"type", "doc",
				"contents", List.of(paragraphNode)
		);

		return new ObjectMapper().writeValueAsString(map);
	}

	@Test
	@DisplayName("댓글 조회 API")
	void findPostById() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment1 = Comment.create(createMockTiptapContent(), user);
		Comment comment2 = Comment.create(createMockTiptapContent(), user);
		post.addComments(List.of(comment1, comment2));
		commentRepository.save(comment1);
		commentRepository.save(comment2);

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/posts/{postId}/comments", post.getId())
											.param("sort", "NEW"))
									.andExpect(status().isOk())
									.andDo(print())
									.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		CommentListResDto response = objectMapper.readValue(responseBody, CommentListResDto.class);
		assertThat(response.hasNext()).isFalse();
	}

	@Test
	@DisplayName("댓글 생성 API")
	void createComment() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		CommentCreateReqDto request = new CommentCreateReqDto(createMockTiptapContent());

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/posts/{postId}/comments", post.getId())
										.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
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
	}

	@Test
	@DisplayName("댓글 수정 API")
	void updateComment() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment = Comment.create(createMockTiptapContent(), user);
		post.addComment(comment);
		commentRepository.save(comment);

		CommentUpdateReqDto request = new CommentUpdateReqDto("Hello Comment");

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}",post.getId(), comment.getId())
										.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
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
	}

	@Test
	@DisplayName("댓글 상태 수정 API")
	void updateCommentStatusApi_fail() throws Exception {
		// given
		User user = createTestUser();
		Post post = createTestPost(user);
		Comment comment = Comment.create(createMockTiptapContent(), user);
		post.addComment(comment);
		Comment savedComment = commentRepository.save(comment);

		CommentStatusUpdateReqDto request = new CommentStatusUpdateReqDto(CommentStatus.DELETED);

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/posts/{postId}/comments/{commentId}/status", post.getId(), savedComment.getId())
											.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(user))
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
