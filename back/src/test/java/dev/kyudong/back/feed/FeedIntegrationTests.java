package dev.kyudong.back.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.testhelper.base.IntegrationTestBase;
import dev.kyudong.back.common.interceptor.GuestIdInterceptor;
import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.feed.api.dto.res.FeedItemResDto;
import dev.kyudong.back.feed.api.dto.res.FeedListResDto;
import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.repository.FollowRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.CategoryRepository;
import dev.kyudong.back.post.adapter.out.persistence.repository.CategoryTranslationRepository;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.CategoryTranslation;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
		"spring.jpa.properties.hibernate.jpa_compliance=false",
		"spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"
})
public class FeedIntegrationTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FollowRepository followRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryTranslationRepository categoryTranslationRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	// 테스트 데이터 (클래스 레벨에서 공유)
	private User testUser;
	private User createUser(String username) {
		return User.builder()
				.username(username)
				.rawPassword("test1234")
				.encodedPassword("test1234")
				.build();
	}

	@BeforeEach
	public void setupTestData() {
		// 사용자 생성 및 저장
		testUser = userRepository.save(createUser("testUser"));
		User follower = userRepository.save(createUser("follower"));

		List<User> randomUsers = IntStream.rangeClosed(1, 50)
				.mapToObj(i -> userRepository.save(createUser("randomUser" + i)))
				.toList();

		// 팔로우 관계 생성
		Follow newFollow = Follow.create(follower, testUser);
		followRepository.save(newFollow);

		Category testCategory = Category.builder()
				.categoryCode("test_1")
				.build();
		categoryRepository.save(testCategory);

		CategoryTranslation testTranslation_ko = CategoryTranslation.builder()
				.category(testCategory)
				.languageCode("ko-KR")
				.name("일상(ko)")
				.build();
		categoryTranslationRepository.save(testTranslation_ko);

		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			String sql = """
                INSERT INTO posts (
                    user_id, subject, content, status, post_view_count,
                    post_score, created_at, modified_at
                ) VALUES (
                    :userId, :subject, CAST(:content AS jsonb), :status, :viewCount,
                    :score, :createdAt, :modifiedAt
                )
                """;

			// 오래된 게시글
			IntStream.range(0, 1200).forEach(i -> {
				User author = randomUsers.get(i % 50);
				Query query = em.createNativeQuery(sql);
				query.setParameter("userId", author.getId());
				query.setParameter("subject", "오래된 게시글 " + i);
				query.setParameter("contents", "{\"text\": \"오래된 게시글 본문 " + i + "\"}");
				query.setParameter("status", PostStatus.NORMAL.name());
				query.setParameter("viewCount", ThreadLocalRandom.current().nextLong(0, 500));
				query.setParameter("score", ThreadLocalRandom.current().nextDouble(-50, 200));
				query.setParameter("categoryId", testCategory.getId());
				query.setParameter("createdAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(60, 365), ChronoUnit.DAYS)));
				query.setParameter("modifiedAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(60, 365), ChronoUnit.DAYS)));
				query.executeUpdate();
			});

			// 인기 게시글
			IntStream.range(0, 300).forEach(i -> {
				User author = randomUsers.get(i % 30);
				Query query = em.createNativeQuery(sql);
				query.setParameter("userId", author.getId());
				query.setParameter("subject", "인기 게시글 " + i);
				query.setParameter("contents", "{\"text\": \"인기 게시글 본문 " + i + "\"}");
				query.setParameter("status", PostStatus.NORMAL.name());
				query.setParameter("viewCount", ThreadLocalRandom.current().nextLong(500, 5000));
				query.setParameter("score", ThreadLocalRandom.current().nextDouble(100, 400));
				query.setParameter("categoryId", testCategory.getId());
				query.setParameter("createdAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(1, 7), ChronoUnit.DAYS)));
				query.setParameter("modifiedAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(1, 7), ChronoUnit.DAYS)));
				query.executeUpdate();
			});

			// 팔로우 게시글
			IntStream.range(0, 20).forEach(i -> {
				Query query = em.createNativeQuery(sql);
				query.setParameter("userId", follower.getId());
				query.setParameter("subject", "팔로우 게시글 " + i);
				query.setParameter("contents", "{\"text\": \"팔로우 게시글 본문 " + i + "\"}");
				query.setParameter("status", PostStatus.NORMAL.name());
				query.setParameter("viewCount", ThreadLocalRandom.current().nextLong(0, 500));
				query.setParameter("score", ThreadLocalRandom.current().nextDouble(-50, 200));
				query.setParameter("categoryId", testCategory.getId());
				query.setParameter("createdAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(1, 90), ChronoUnit.DAYS)));
				query.setParameter("modifiedAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(1, 90), ChronoUnit.DAYS)));
				query.executeUpdate();
			});

			// 최신 게시글
			IntStream.range(0, 100).forEach(i -> {
				User author = randomUsers.get(i % 30);
				Query query = em.createNativeQuery(sql);
				query.setParameter("userId", author.getId());
				query.setParameter("subject", "최신 게시글 " + i);
				query.setParameter("contents", "{\"text\": \"최신 게시글 본문 " + i + "\"}");
				query.setParameter("status", PostStatus.NORMAL.name());
				query.setParameter("viewCount", ThreadLocalRandom.current().nextLong(10, 1000));
				query.setParameter("score", ThreadLocalRandom.current().nextDouble(-50, 50));
				query.setParameter("categoryId", testCategory.getId());
				query.setParameter("createdAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(0, 6), ChronoUnit.HOURS)));
				query.setParameter("modifiedAt", Timestamp.from(Instant.now().minus(ThreadLocalRandom.current().nextInt(0, 6), ChronoUnit.HOURS)));
				query.executeUpdate();
			});

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	@AfterEach
	void tearDown() {
		postRepository.deleteAll();
		followRepository.deleteAll();
		userRepository.deleteAll();
		categoryTranslationRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	@DisplayName("사용자 피드 조회")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void findFeeds_withUser() throws Exception {
		// given
		String feedKey = "feed:user:" + testUser.getId();
		redissonClient.getList(feedKey).clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/feeds")
								.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(testUser)))
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		// then
		Awaitility.await()
				.atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					RList<String> feedCache = redissonClient.getList(feedKey);
					return !feedCache.isEmpty();
				});

		String responseBody = result.getResponse().getContentAsString();
		FeedListResDto response = objectMapper.readValue(responseBody, FeedListResDto.class);
		assertThat(response.content()).doesNotHaveDuplicates();

		RList<String> feedCache = redissonClient.getList(feedKey);
		assertThat(feedCache.readAll()).isNotEmpty();
	}

	@Test
	@DisplayName("사용자 피드 조회 - 페이징")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void findFeeds_withUser_paging() throws Exception {
		// given
		String feedKey = "feed:user:" + testUser.getId();
		redissonClient.getList(feedKey).clear();

		MvcResult result = mockMvc.perform(get("/api/v1/feeds")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(testUser)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		Awaitility.await()
				.atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					RList<String> feedCache = redissonClient.getList(feedKey);
					return !feedCache.isEmpty();
				});

		String responseBody = result.getResponse().getContentAsString();
		FeedListResDto response = objectMapper.readValue(responseBody, FeedListResDto.class);
		List<FeedItemResDto> firstPage = response.content();

		int page = response.nextPage();

		// when
		result = mockMvc.perform(get("/api/v1/feeds")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.createAccessToken(testUser))
						.param("page", String.valueOf(page)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		responseBody = result.getResponse().getContentAsString();
		response = objectMapper.readValue(responseBody, FeedListResDto.class);
		List<FeedItemResDto> secondPage = response.content();

		assertThat(firstPage).isNotEqualTo(secondPage);
	}

	@Test
	@DisplayName("게스트 피드 조회")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void findFeeds_withGuest() throws Exception {
		// given
		String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		String feedKey = "feed:guest:" + guestId;
		redissonClient.getList(feedKey).clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/v1/feeds")
						.cookie(cookie))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		Awaitility.await()
				.atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					RList<String> feedCache = redissonClient.getList(feedKey);
					return !feedCache.isEmpty();
				});

		String responseBody = result.getResponse().getContentAsString();
		FeedListResDto response = objectMapper.readValue(responseBody, FeedListResDto.class);
		assertThat(response.content()).doesNotHaveDuplicates();

		RList<String> feedCache = redissonClient.getList(feedKey);
		assertThat(feedCache.readAll()).isNotEmpty();
	}

	@Test
	@DisplayName("게스트 피드 조회 - 페이징")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void findFeeds_withGuest_paging() throws Exception {
		// given
		String guestId = UUID.randomUUID().toString();
		Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_ID_COOKIE_NAME, guestId);
		String feedKey = "feed:guest:" + guestId;
		redissonClient.getList(feedKey).clear();

		MvcResult result = mockMvc.perform(get("/api/v1/feeds")
						.cookie(cookie))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		Awaitility.await()
				.atMost(5, TimeUnit.SECONDS)
				.pollInterval(100, TimeUnit.MILLISECONDS)
				.until(() -> {
					RList<String> feedCache = redissonClient.getList(feedKey);
					return !feedCache.isEmpty();
				});

		String responseBody = result.getResponse().getContentAsString();
		FeedListResDto response = objectMapper.readValue(responseBody, FeedListResDto.class);
		List<FeedItemResDto> firstPage = response.content();

		int page = response.nextPage();

		// when
		result = mockMvc.perform(get("/api/v1/feeds")
						.cookie(cookie)
						.param("page", String.valueOf(page)))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn();

		// then
		responseBody = result.getResponse().getContentAsString();
		response = objectMapper.readValue(responseBody, FeedListResDto.class);
		List<FeedItemResDto> secondPage = response.content();

		assertThat(firstPage).isNotEqualTo(secondPage);
	}

}
