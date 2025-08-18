package dev.kyudong.back.post.service;

import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.api.dto.req.PostCreateReqDto;
import dev.kyudong.back.post.api.dto.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.PostUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.PostCreateResDto;
import dev.kyudong.back.post.api.dto.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.api.dto.res.PostDetailResDto;
import dev.kyudong.back.post.api.dto.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public PostDetailResDto findPostById(long postId) {
		log.debug("게시글 조회 요청 시작: postId: {}", postId);

		Post post = postRepository.findById(postId).orElseThrow(() -> {
					log.warn("게시글 조회 실패 - 존재하지 않는 게시글 : postId: {}", postId);
					return new PostNotFoundException("Post {" + postId + "} Not Found");
				});

		log.info("게시글 조회 성공: postId: {}", postId);
		return PostDetailResDto.from(post);
	}

	@Transactional
	public PostCreateResDto createPost(PostCreateReqDto request) {
		log.debug("게시글 생성 요청 시작: userId: {}, subject: {}", request.userId(), request.subject());

		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> {
					log.warn("사용자 조회 실패 - 존재하지 않는 사용자 : userId: {}", request.userId());
					return new UserNotFoundException("User {" + request.userId() + "} Not Found");
				});

		Post newPost = Post.builder()
				.subject(request.subject())
				.content(request.content())
				.build();
		user.addPost(newPost);
		Post savedPost = postRepository.save(newPost);

		log.info("게시글 생성 성공: userId: {}, postId: {}", savedPost.getUser().getId(), savedPost.getId());
		return PostCreateResDto.from(savedPost);
	}

	@Transactional
	public PostUpdateResDto updatePost(long postId, PostUpdateReqDto request) {
		log.debug("게시글 수정 요청 시작: userId: {}, postId: {}", request.userId(), postId);

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> {
					log.warn("게시글 수정 실패 - 존재하지 않는 게시글 : postId: {}", postId);
					return new PostNotFoundException("Post {" + postId + "} Not Found");
				});

		if (!post.getUser().getId().equals(request.userId())) {
			log.warn("게시글 수정 실패 - 권한 없음 : userId: {}, postId: {}", request.userId(), postId);
			throw new InvalidAccessException("User {" + request.userId() + "} has no permission to update post " + postId);
		}

		if (StringUtils.hasText(request.subject())) {
			post.updateSubject(request.subject());
		}

		if (StringUtils.hasText(request.content())) {
			post.updateContent(request.content());
		}

		log.info("게시글 수정 성공: userId: {}, postId: {}", post.getUser().getId(), post.getId());
		return PostUpdateResDto.from(post);
	}

	@Transactional
	public PostStatusUpdateResDto updatePostStatus(long postId, PostStatusUpdateReqDto request) {
		log.debug("게시글 상태 수정 요청 시작: userId: {}, postId: {}", request.userId(), postId);

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> {
					log.warn("게시글 상태 수정 실패 - 존재하지 않는 게시글 : postId: {}", postId);
					return new PostNotFoundException("Post {" + postId + "} Not Found");
				});

		if (!post.getUser().getId().equals(request.userId())) {
			log.warn("게시글 상태 수정 실패 - 권한 없음 : userId: {}, postId: {}", request.userId(), postId);
			throw new InvalidAccessException("User {" + request.userId() + "} has no permission to update post status " + postId);
		}

		switch (request.status()) {
			case NORMAL -> post. restore();
			case DELETED -> post.delete();
			default -> {
				log.warn("응답할 수 없는 게시글 상태 요청 : postId: {}, status: {}", post, request.status().name());
				throw new InvalidInputException("PostStatus Cant not be update");
			}
		}

		log.info("게시글 상태 수정 성공: userId: {}, postId: {}, status: {}", post.getUser().getId(), post.getId(), post.getStatus().name());
		return PostStatusUpdateResDto.from(post);
	}

}
