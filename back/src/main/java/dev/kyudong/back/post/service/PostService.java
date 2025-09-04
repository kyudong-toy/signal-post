package dev.kyudong.back.post.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.api.dto.event.PostCreateFeedEvent;
import dev.kyudong.back.post.api.dto.event.PostCreateFileEvent;
import dev.kyudong.back.post.api.dto.event.PostCreateNotification;
import dev.kyudong.back.post.api.dto.event.PostUpdateFileEvent;
import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.domain.PostStatus;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final ObjectMapper objectMapper;

	@Transactional(readOnly = true)
	public PostDetailResDto findPostById(long postId) {
		log.debug("게시글 조회를 시작합니다: postId={}", postId);

		Post post = postRepository.findById(postId).orElseThrow(() -> {
					log.warn("존재하지 않는 게시글입니다: postId={}", postId);
					return new PostNotFoundException(postId);
				});

		log.info("게시글을 조회했습니다: postId={}", postId);
		return PostDetailResDto.from(post);
	}

	@Transactional
	public PostCreateResDto createPost(final Long userId, PostCreateReqDto request) {
		log.debug("게시글 생성을 시작합니다: userId={}, subject={}", userId, request.subject());

		if (!userRepository.existsById(userId)) {
			log.warn("존재하지 않는 사용자입니다: userId={}", userId);
			throw new UserNotFoundException("User: {"+ userId + "} Not Found");
		}
		User user = userRepository.getReferenceById(userId);

		String content;
		try {
			content = objectMapper.writeValueAsString(request.content());
		} catch (JsonProcessingException j) {
			throw new RuntimeException("서버 에러 발생");
		}

		Post newPost = Post.builder()
				.subject(request.subject())
				.content(content)
				.build();
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);

		final Long postId = savedPost.getId();
		if (!request.fileIds().isEmpty()) {
			log.debug("게시글에서 파일 저장 이벤트를 시작합니다: userId={}, postId={}", userId, postId);
			PostCreateFileEvent fileEvent = new PostCreateFileEvent(postId, request.fileIds());
			applicationEventPublisher.publishEvent(fileEvent);
		}

		// feed 추가
		PostCreateFeedEvent feedEvent = new PostCreateFeedEvent(savedPost, user);
		applicationEventPublisher.publishEvent(feedEvent);

		// 알림 이벤트 추가
		PostCreateNotification notificationEvent = new PostCreateNotification(postId, userId);
		applicationEventPublisher.publishEvent(notificationEvent);

		log.info("게시글 생성에 성공했습니다: userId={}, postId={}", userId, postId);
		return PostCreateResDto.from(savedPost);
	}

	@Transactional
	public PostUpdateResDto updatePost(final Long postId, final Long userId, PostUpdateReqDto request) {
		log.debug("게시글 수정을 시작합니다: userId={}, postId={}", userId, postId);

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 게시글입니다: postId={}", postId);
					return new PostNotFoundException(postId);
				});

		User user = userRepository.getReferenceById(userId);
		if (!post.getUser().equals(user)) {
			log.warn("게시글 수정 권한이 없습니다: userId={}, postId={}", userId, postId);
			throw new InvalidAccessException("User {" + userId + "} has no permission to update post " + postId);
		}

		if (StringUtils.hasText(request.subject())) {
			post.updateSubject(request.subject());
		}

		try {
			String content = objectMapper.writeValueAsString(request.content());
			if (StringUtils.hasText(content)) {
				post.updateContent(content);
			}
		} catch (JsonProcessingException j) {
			throw new RuntimeException("서버 에러 발생");
		}

		if (!request.fileIds().isEmpty()) {
			log.debug("게시글에서 파일 업데이트 이벤트를 시작합니다: userId={}, postId={}", userId, postId);
			PostUpdateFileEvent fileEvent = new PostUpdateFileEvent(postId, request.fileIds(), request.delFileIds());
			applicationEventPublisher.publishEvent(fileEvent);
		}

		// feed 추가
		PostCreateFeedEvent feedEvent = new PostCreateFeedEvent(post, user);
		applicationEventPublisher.publishEvent(feedEvent);

		log.info("게시글 수정 요청 성공: userId={}, postId={}", userId, post.getId());
		return PostUpdateResDto.from(post);
	}

	@Transactional
	public PostStatusUpdateResDto updatePostStatus(final Long postId, final Long userId, PostStatusUpdateReqDto request) {
		log.debug("게시글 상태 수정 요청 시작: userId: {}, postId: {}", userId, postId);

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> {
					log.warn("존재하지 않는 게시글입니다: postId={}", postId);
					return new PostNotFoundException(postId);
				});

		if (!post.getUser().getId().equals(userId)) {
			log.warn("권한 부족으로 게시글 상태 수정이 취소되었습니다: userId={}, postId={}", userId, postId);
			throw new InvalidAccessException("User {" + userId + "} has no permission to update post status " + postId);
		}

		PostStatus prevStatus = post.getStatus();
		switch (request.status()) {
			case NORMAL -> post. restore();
			case DELETED -> post.delete();
			default -> {
				log.warn("허용되지 않는 상태값입니다: postId={}, status={}", post, request.status().name());
				throw new InvalidInputException("PostStatus Cant not be update");
			}
		}

		log.info("게시글 상태를 수정했습지다: userId={}, postId={}, prevStatus={} curStatus={}", post.getUser().getId(), post.getId(), prevStatus, post.getStatus().name());
		return PostStatusUpdateResDto.from(post);
	}

}
