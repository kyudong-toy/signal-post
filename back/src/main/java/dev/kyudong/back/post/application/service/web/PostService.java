package dev.kyudong.back.post.application.service.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.application.port.in.web.CategoryUsecase;
import dev.kyudong.back.post.application.port.in.web.TagUsecase;
import dev.kyudong.back.post.application.port.out.event.PostEventPublishPort;
import dev.kyudong.back.post.application.port.out.event.PostViewEventPublishPort;
import dev.kyudong.back.post.application.port.out.web.PostPersistencePort;
import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.domain.entity.PostStatus;
import dev.kyudong.back.post.domain.entity.Tag;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService implements PostUsecase {

	private final ObjectMapper objectMapper;
	private final UserService userService;
	private final PostEventPublishPort postEventPublishPort;
	private final PostViewEventPublishPort postViewEventPublishPort;
	private final PostPersistencePort postPersistencePort;
	private final TagUsecase tagUsecase;
	private final CategoryUsecase categoryUsecase;
	private final RedissonClient redissonClient;

	@Override
	@Transactional(readOnly = true)
	public PostDetailResDto findPostByIdWithUser(Long userId, Long postId) {
		log.debug("사용자가 게시글 조회를 시작합니다: postId={}", postId);

		Post post = postPersistencePort.findByIdOrThrow(postId);

		if (userId !=  null && !userId.equals(post.getUser().getId())) {
			User user = userService.getUserProxy(userId);
			postViewEventPublishPort.increasePostViewWithUser(user, post);
			redissonClient.getBloomFilter("feed_seen:user" + userId).add(postId);
		}

		return PostDetailResDto.from(post);
	}

	@Override
	@Transactional(readOnly = true)
	public PostDetailResDto findPostByIdWithGuest(String guestId, Long postId) {
		log.debug("게스트가 게시글 조회를 시작합니다: postId={}", postId);

		Post post = postPersistencePort.findByIdOrThrow(postId);
		postViewEventPublishPort.increasePostViewWithGuest(guestId, post);
		redissonClient.getBloomFilter("feed_seen:guest" + guestId).add(postId);

		return PostDetailResDto.from(post);
	}

	@Override
	public List<Post> findRecentPostsWithUser(User user, Instant now, int size) {
		return postPersistencePort.findRecentPostsWithUser(user, now, size);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Post> findRecentPostsWithGuest(Instant now, int size) {
		return postPersistencePort.findRecentPostsWithGuest(now, size);
	}

	@Override
	public List<Post> findPopularPostsWithUser(User user, Instant now, int size) {
		return postPersistencePort.findPopularPostsWithUser(user, now, size);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Post> findPopularPostsWithGuest(Instant now, int size) {
		return postPersistencePort.findPopularPostsWithGuest(now, size);
	}

	@Override
	public List<Post> findByFollowingPost(User user, Instant now, int size) {
		return postPersistencePort.findByFollowingPost(user, now, size);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Post> findAllByIds(Set<Long> postIds) {
		return postPersistencePort.findAllByIds(postIds);
	}

	@Override
	@Transactional
	public PostCreateResDto createPost(Long userId, PostCreateReqDto request) {
		log.debug("게시글 생성을 시작합니다: userId={}, subject={}", userId, request.subject());

		Category category = categoryUsecase.findByCategoryCode(request.categoryCode());
		Post newPost = Post.of(
				request.subject(),
				conventContentJsonToString(request.content()),
				category
		);
		User user = userService.getUserProxy(userId);
		user.addPost(newPost);

		Post savedPost = postPersistencePort.save(newPost);

		Set<Tag> newTags = tagUsecase.caretedNewTag(request.tags());
		savedPost.addTags(newTags);

		postEventPublishPort.postCreateEventPublish(savedPost, request.fileIds());

		log.info("게시글 생성에 성공했습니다: postId={}", savedPost.getId());
		return PostCreateResDto.from(savedPost);
	}

	@Override
	@Transactional
	public PostUpdateResDto updatePost(Long postId, Long userId, PostUpdateReqDto request) {
		log.debug("게시글 수정을 시작합니다: userId={}, postId={}", userId, postId);

		Post post = postPersistencePort.findByIdOrThrow(postId);
		validatePermission(post.getUser().getId(), userId, postId);

		post.updateSubject(request.subject());
		post.updateContent(conventContentJsonToString(request.content()));

		Set<Tag> tags = tagUsecase.caretedNewTag(request.tags());
		post.addTags(tags);

		postEventPublishPort.postUpdateEventPublish(post, request.fileIds());

		log.info("게시글 수정 요청 성공: userId={}, postId={}", userId, post.getId());
		return PostUpdateResDto.from(post);
	}

	@Override
	@Transactional
	public PostStatusUpdateResDto updatePostStatus(Long postId, Long userId, PostStatusUpdateReqDto request) {
		log.debug("게시글 상태 수정 요청 시작: userId: {}, postId: {}", userId, postId);

		Post post = postPersistencePort.findByIdOrThrow(postId);
		validatePermission(post.getUser().getId(), userId, postId);

		PostStatus prevStatus = post.getStatus();
		switch (request.status()) {
			case NORMAL -> post.restore();
			case DELETED -> post.delete();
			default -> {
				log.warn("허용되지 않는 상태값입니다: postId={}, status={}", post, request.status().name());
				throw new InvalidInputException("PostStatus Cant not be update");
			}
		}

		log.info("게시글 상태를 수정했습니다: userId={}, postId={}, prevStatus={} curStatus={}", post.getUser().getId(), post.getId(), prevStatus, post.getStatus().name());
		return PostStatusUpdateResDto.from(post);
	}

	@Override
	@Transactional
	public void refreshRandomOldPost() {
		postPersistencePort.refreshRandomOldPost();
	}

	/**
	 * 게시글을 확인하고 없다면 {@link PostNotFoundException}을 던집니다
	 * @param postId	확인할 게시글
	 * @throws PostNotFoundException 게시글이 없다면
	 */
	@Override
	public void validatePostExists(Long postId) {
		if (!postPersistencePort.existsById(postId)) {
			log.warn("존재하지 않는 게시글 : postId: {}", postId);
			throw new PostNotFoundException(postId);
		}
	}

	/**
	 * 게시글 엔티티를 가져오고 없다면 {@link PostNotFoundException}을 던집니다
	 * @param postId	가져올 게시글
	 * @return Post		엔티티
	 * @throws PostNotFoundException 게시글이 없다면
	 */
	@Override
	@Transactional(readOnly = true)
	public Post getPostEntityOrThrow(Long postId) {
		log.debug("게시글 조회를 시작합니다: postId={}", postId);
		return postPersistencePort.findByIdOrThrow(postId);
	}

	/**
	 * 에디터 본문(Object)를 String으로 변환합니다.
	 * @param content	에디터 본문 내용
	 * @return String	변환된 본문
	 */
	private String conventContentJsonToString(Object content) {
		try {
			return objectMapper.writeValueAsString(content);
		} catch (JsonProcessingException j) {
			log.error("게시글 본문 파싱에 실패했습니다");
			log.debug("content={}", content);
			throw new RuntimeException("서버 에러 발생");
		}
	}

	/**
	 * 게시글 권한을 확인하고 권한이 없을 경우 {@link AccessDeniedException}를 던집니다
	 * @param postUserId		게시글에 저장된 사용자 아이디
	 * @param requestUserId		수정을 요청한 사용자 아이디
	 * @param postId			게시글 아이디
	 */
	private void validatePermission(Long postUserId, Long requestUserId, Long postId) {
		if (!postUserId.equals(requestUserId)) {
			log.warn("게시글 수정 권한이 없습니다: userId={}, postId={}", requestUserId, postId);
			throw new AccessDeniedException(String.format("게시글 수정권한이 없습니다: requestUserId={%d}, postId={%d}", requestUserId, postId));
		}
	}

}
