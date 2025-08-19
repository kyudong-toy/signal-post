package dev.kyudong.back.post.service;

import dev.kyudong.back.common.exception.InvalidAccessException;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.api.dto.req.CommentCreateReqDto;
import dev.kyudong.back.post.api.dto.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.api.dto.req.CommentUpdateReqDto;
import dev.kyudong.back.post.api.dto.res.*;
import dev.kyudong.back.post.domain.Comment;
import dev.kyudong.back.post.domain.Post;
import dev.kyudong.back.post.exception.CommentNotFoundException;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.post.repository.CommentRepository;
import dev.kyudong.back.post.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<CommentDetailResDto> findCommentsByPostId(final Long postId) {
		log.debug("댓글 목록 조회 요청 시작: postId: {}", postId);

		List<Comment> commentList = commentRepository.findByPostId(postId);

		log.info("댓글 조회 요청 성공: postId: {}, size: {}", postId, commentList.size());
		return commentList.stream()
				.map(CommentDetailResDto::from)
				.toList();
	}


	@Transactional
	public CommentCreateResDto createComment(final Long postId, final Long userId, CommentCreateReqDto request) {
		log.debug("댓글 생성 요청 시작: postId: {}, userId: {}", postId, userId);

		if (!postRepository.existsById(postId)) {
			log.warn("댓글 생성 실패 - 존재하지 않는 게시글 : postId: {}", postId);
			throw new PostNotFoundException(postId);
		}
		Post post = postRepository.getReferenceById(postId);

		if (!userRepository.existsById(userId)) {
			log.warn("사용자 조회 실패 - 존재하지 않는 사용자 : id: {}", userId);
			throw new UserNotFoundException(userId);
		}
		User user = userRepository.getReferenceById(userId);

		Comment newComment = Comment.builder()
				.content(request.content())
				.user(user)
				.build();
		post.addComment(newComment);
		Comment savedComment = commentRepository.save(newComment);

		log.info("댓글 생성 성공: postId: {}, commentId: {}", postId, savedComment.getId());
		return CommentCreateResDto.from(savedComment);
	}

	@Transactional
	public CommentUpdateResDto updateComment(final Long postId, final Long commentId, final Long userId, CommentUpdateReqDto request) {
		log.debug("댓글 수정 요청 시작: userId: {}, commentId: {}", userId, commentId);

		if (!postRepository.existsById(postId)) {
			log.warn("댓글 수정 실패 - 존재하지 않는 게시글 : postId: {}", postId);
			throw new PostNotFoundException(postId);
		}

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> {
					log.warn("댓글 조회 실패 - 존재하지 않는 댓글 : postId: {}, commentId: {}", postId, commentId);
					return new CommentNotFoundException(commentId);
				});

		if (!comment.getUser().getId().equals(userId)) {
			log.warn("댓글 수정 실패 - 권한 없음 : userId: {}, postId: {}", userId, postId);
			throw new InvalidAccessException("User {" + userId + "} has no permission to update Comment " + commentId);
		}

		if (StringUtils.hasText(request.content())) {
			comment.updateContent(request.content());
		}

		log.info("댓글 수정 요청 성공: postId: {}, commentId: {}", postId, commentId);
		return CommentUpdateResDto.from(comment);
	}

	@Transactional
	public CommentStatusUpdateResDto updateCommentStatus(final Long postId, final Long commentId, final Long userId, CommentStatusUpdateReqDto request) {
		log.debug("댓글 상태 수정 요청 시작: postId: {}, commentId: {}", postId, commentId);

		if (!postRepository.existsById(postId)) {
			log.warn("댓글 상태 수정 실패 - 존재하지 않는 게시글 : postId: {}", postId);
			throw new PostNotFoundException(postId);
		}

		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> {
					log.warn("댓글 상태 수정 실패 - 존재하지 않는 댓글 : commentId: {}", commentId);
					return new CommentNotFoundException(commentId);
				});

		if (!comment.getUser().getId().equals(userId)) {
			log.warn("댓글 상태 수정 실패 - 권한 없음 : userId: {}, commentId: {}", userId, commentId);
			throw new InvalidAccessException("User {" + userId + "} has no permission to update Comment status " + commentId);
		}

		switch (request.status()) {
			case NORMAL -> comment.restore();
			case DELETED -> comment.delete();
			default -> {
				log.warn("응답할 수 없는 댓글 상태 요청 : postId: {}, status: {}", comment, request.status().name());
				throw new InvalidInputException("CommentStatus Cant not be update");
			}
		}

		log.info("댓글 상태 수정 성공: userId: {}, postId: {}, status: {}", userId, comment.getId(), comment.getStatus().name());
		return CommentStatusUpdateResDto.from(comment);
	}

}
