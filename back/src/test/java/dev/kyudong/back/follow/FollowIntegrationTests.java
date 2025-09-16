package dev.kyudong.back.follow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.follow.api.res.FollowCreateResDto;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FollowIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FollowRepository followRepository;

	@Autowired
	private JwtUtil jwtUtil;

	private User makeMockUser(String username) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("password")
				.encodedPassword("password")
				.build();
		return userRepository.save(mockUser);
	}

	@Test
	@DisplayName("팔로우 요청")
	void createFollow() throws Exception {
		// given
		User mockFollower = makeMockUser("zxzcz12");
		User mockFollowing = makeMockUser("ckznds");

		// when
		MvcResult result = mockMvc.perform(post("/api/v1/users/{username}/follow", mockFollowing.getUsername())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(mockFollower)))
				.andExpect(status().isCreated())
				.andDo(print())
				.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FollowCreateResDto response = objectMapper.readValue(responseBody, FollowCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.PENDING);
	}

	@Test
	@DisplayName("팔로우 승낙")
	void acceptFollow() throws Exception {
		// given
		User mockFollower = makeMockUser("zxzcz12");
		User mockFollowing = makeMockUser("ckznds");

		Follow follow = Follow.create(mockFollower, mockFollowing);
		followRepository.save(follow);

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/users/{username}/accept", mockFollowing.getUsername())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(mockFollower)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FollowCreateResDto response = objectMapper.readValue(responseBody, FollowCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.FOLLOWING);
	}

	@Test
	@DisplayName("팔로우 차단")
	void blokcFollow() throws Exception {
		// given
		User mockFollower = makeMockUser("zxzcz12");
		User mockFollowing = makeMockUser("ckznds");

		Follow follow = Follow.create(mockFollower, mockFollowing);
		follow.accept();
		followRepository.save(follow);

		// when
		MvcResult result = mockMvc.perform(patch("/api/v1/users/{username}/block", mockFollowing.getUsername())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(mockFollower)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FollowCreateResDto response = objectMapper.readValue(responseBody, FollowCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.BLOCKED);
	}

	@Test
	@DisplayName("팔로우 삭제")
	void deleteFollow() throws Exception {
		// given
		User mockFollower = makeMockUser("zxzcz12");
		User mockFollowing = makeMockUser("ckznds");

		Follow follow = Follow.create(mockFollower, mockFollowing);
		follow.accept();
		followRepository.save(follow);

		// when
		MvcResult result = mockMvc.perform(delete("/api/v1/users/{username}/follow", mockFollowing.getUsername())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(mockFollower)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString();
		FollowCreateResDto response = objectMapper.readValue(responseBody, FollowCreateResDto.class);
		assertThat(response).isNotNull();
		assertThat(response.followerId()).isEqualTo(mockFollower.getId());
		assertThat(response.followingId()).isEqualTo(mockFollowing.getId());
		assertThat(response.status()).isEqualTo(FollowStatus.UNFOLLOWED);
	}

}
