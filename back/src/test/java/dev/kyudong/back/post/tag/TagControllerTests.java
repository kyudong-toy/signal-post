package dev.kyudong.back.post.tag;

import dev.kyudong.back.common.config.SecurityConfig;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.post.adapter.in.web.TagController;
import dev.kyudong.back.post.application.port.in.web.TagUsecase;
import dev.kyudong.back.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import(SecurityConfig.class)
public class TagControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@SuppressWarnings("unused")
	@MockitoBean
	private TagUsecase tagUsecase;

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtUtil jwtUtil;

	@SuppressWarnings("unused")
	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	@DisplayName("태그 조회 API - 성공")
	@WithMockCustomUser
	void findPostByIdApi_success() throws Exception {
		// given
		final String query = "스";
		List<String> response = List.of("스프", "스시", "스프링");
		given(tagUsecase.findTagNamesByQuery(anyString())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/v1/tags/search")
						.param("query", query))
				.andExpect(status().isOk())
				.andDo(print());
	}

}
