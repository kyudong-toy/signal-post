package dev.kyudong.back.post.tag;

import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.adapter.out.persistence.repository.TagRepository;
import dev.kyudong.back.post.domain.entity.*;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class TagIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		Tag tag1 = Tag.of("스프링");
		Tag tag2 = Tag.of("스프");
		Tag tag3 = Tag.of("스시");
		Tag tag4 = Tag.of("자바");
		Tag tag5 = Tag.of("Node");

		tagRepository.saveAll(List.of(tag1, tag2, tag3, tag4, tag5));
	}

	private User createTestUser() {
		User newUser = User.builder()
				.username("mockUser")
				.rawPassword("password")
				.encodedPassword("password")
				.build();
		return userRepository.save(newUser);
	}

	@Test
	@DisplayName("게시글 조회 API")
	void findPostById() throws Exception {
		// given
		User user = createTestUser();
		String query = "스";

		// when & then
		mockMvc.perform(get("/api/v1/tags/search")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(user))
						.param("query", query))
				.andExpect(status().isOk())
				.andDo(print());
	}

}
