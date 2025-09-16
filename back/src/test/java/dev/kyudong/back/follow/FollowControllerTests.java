package dev.kyudong.back.follow;

import dev.kyudong.back.follow.api.res.FollowRelationResDto;
import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.follow.api.FollowController;
import dev.kyudong.back.follow.api.res.FollowCreateResDto;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.service.FollowService;
import dev.kyudong.back.testhelper.security.WithMockCustomUser;
import dev.kyudong.back.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.*;

@WebMvcTest(FollowController.class)
@Import(SecurityConfig.class)
public class FollowControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private FollowService followService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	private static User makeMockUser(Long userId, String username) {
		User mockUser = User.builder()
				.username(username)
				.rawPassword("rawPassword")
				.encodedPassword("encodedPassword")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", userId);
		return mockUser;
	}

	@Test
	@DisplayName("팔로우 요청 API - 성공")
	@WithMockCustomUser(username = "dnz1dd")
	void createFollowApi_success() throws Exception {
		// given
		final Long followerId = 1L;
		User mockFollowing = makeMockUser(2L, "ckznds");
		FollowCreateResDto response = new FollowCreateResDto(1L, followerId, mockFollowing.getId(), FollowStatus.PENDING);
		given(followService.requestFollow(followerId, mockFollowing.getUsername())).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/users/{username}/follow", mockFollowing.getUsername()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.followerId").value(followerId))
				.andExpect(jsonPath("$.followingId").value(mockFollowing.getId()))
				.andExpect(jsonPath("$.status").value(FollowStatus.PENDING.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("팔로우 요청 API - 실패")
	@WithMockCustomUser
	void createFollowApi_fail_userNotFound() throws Exception {
		// given
		User mockFollower = makeMockUser(1L, "dnz1dd");
		User mockFollowing = makeMockUser(2L, "ckznds");
		given(followService.requestFollow(mockFollower.getId(), mockFollowing.getUsername()))
				.willThrow(AlreadyFollowException.class);

		// when & then
		mockMvc.perform(post("/api/v1/users/{username}/follow", mockFollowing.getUsername()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Already Follow Relation"))
				.andExpect(jsonPath("$.status").value(400))
				.andDo(print());
	}

	@Test
	@DisplayName("팔로우 승낙 API - 성공")
	@WithMockCustomUser(username = "dnz1dd")
	void acceptFollowApi_success() throws Exception {
		// given
		final Long followerId = 1L;
		User mockFollowing = makeMockUser(2L, "ckznds");
		FollowRelationResDto response = new FollowRelationResDto(1L, followerId, mockFollowing.getId(), FollowStatus.FOLLOWING);
		given(followService.accept(followerId, mockFollowing.getUsername())).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/users/{username}/accept", mockFollowing.getUsername()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.followerId").value(followerId))
				.andExpect(jsonPath("$.followingId").value(mockFollowing.getId()))
				.andExpect(jsonPath("$.status").value(FollowStatus.FOLLOWING.name()))
				.andDo(print());
	}

	@Test
	@DisplayName("팔로우 승낙 API - 실패")
	@WithMockCustomUser
	void acceptFollowwApi_fail_userNotFound() throws Exception {
		// given
		User mockFollower = makeMockUser(1L, "dnz1dd");
		User mockFollowing = makeMockUser(2L, "ckznds");
		given(followService.requestFollow(mockFollower.getId(), mockFollowing.getUsername()))
				.willThrow(AlreadyFollowException.class);

		// when & then
		mockMvc.perform(post("/api/v1/users/{username}/follow", mockFollowing.getUsername()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Already Follow Relation"))
				.andExpect(jsonPath("$.status").value(400))
				.andDo(print());
	}

}
