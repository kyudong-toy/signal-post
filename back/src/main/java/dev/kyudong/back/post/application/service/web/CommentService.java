package dev.kyudong.back.post.application.service.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kyudong.back.common.exception.InvalidInputException;
import dev.kyudong.back.post.application.port.in.web.CommentUsecase;
import dev.kyudong.back.post.application.port.in.web.PostUsecase;
import dev.kyudong.back.post.application.port.out.web.CommentPersistencePort;
import dev.kyudong.back.post.application.port.out.web.CommentQueryPort;
import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
import dev.kyudong.back.post.domain.entity.Comment;
import dev.kyudong.back.post.domain.entity.CommentSort;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService implements CommentUsecase {

	private final ObjectMapper objectMapper;
	private final UserRepository userRepository;
	private final PostUsecase postUsecase;
	private final CommentPersistencePort commentPersistencePort;
	private final CommentQueryPort commentQueryPort;

	@Transactional(readOnly = true)
	public CommentListResDto findComments(Long postId, Long cursorId, CommentSort sort) {
		log.debug("댓글 목록 조회 요청 시작: postId: {}", postId);

		List<Comment> commentList = sort.equals(CommentSort.NEW)
			 ? commentQueryPort.findByNewCommentByCursor(postId, cursorId)
			 : commentQueryPort.findByOldCommentByCursor(postId, cursorId);

		if (commentList.isEmpty()) {
			return CommentListResDto.empty();
		}

		log.info("댓글 조회 요청 성공: postId: {}, size: {}", postId, commentList.size());
		return CommentListResDto.from(commentList);
	}

	@Override
	@Transactional
	public CommentCreateResDto createComment(Long postId, Long userId, CommentCreateReqDto request) {
		log.debug("댓글 생성 요청 시작: postId: {}, userId: {}", postId, userId);

		if (!userRepository.existsById(userId)) {
			log.warn("사용자 조회 실패 - 존재하지 않는 사용자 : id: {}", userId);
			throw new UserNotFoundException(userId);
		}
		User user = userRepository.getReferenceById(userId);

		Post post = postUsecase.getPostEntityOrThrow(postId);
		String content = conventContentJsonToString(request.content());
		Comment newComment = Comment.builder()
				.content(content)
				.user(user)
				.build();
		post.addComment(newComment);
		Comment savedComment = commentPersistencePort.save(newComment);

		log.info("댓글 생성 성공: postId: {}, commentId: {}", postId, savedComment.getId());
		return CommentCreateResDto.from(savedComment);
	}

	@Override
	@Transactional
	public CommentUpdateResDto updateComment(final Long postId, final Long commentId, final Long userId, CommentUpdateReqDto request) {
		log.debug("댓글 수정 요청 시작: userId: {}, commentId: {}", userId, commentId);

		postUsecase.validatePostExists(postId);
		Comment comment = commentPersistencePort.findByIdOrThrow(commentId);

		validatePermission(comment.getUser().getId(), userId, commentId);

		String content = conventContentJsonToString(request.content());
		comment.updateContent(content);

		log.info("댓글 수정 요청 성공: postId: {}, commentId: {}", postId, commentId);
		return CommentUpdateResDto.from(comment);
	}

	@Override
	@Transactional
	public CommentStatusUpdateResDto updateCommentStatus(final Long postId, final Long commentId, final Long userId, CommentStatusUpdateReqDto request) {
		log.debug("댓글 상태 수정 요청 시작: postId: {}, commentId: {}", postId, commentId);

		postUsecase.validatePostExists(postId);
		Comment comment = commentPersistencePort.findByIdOrThrow(commentId);

		validatePermission(comment.getUser().getId(), userId, commentId);

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

	/**
	 * 댓글 권한을 확인하고 권한이 없을 경우 {@link AccessDeniedException}를 던집니다
	 * @param commentUserId		게시글에 저장된 사용자 아이디
	 * @param requestUserId		수정을 요청한 사용자 아이디
	 * @param commentId			게시글 아이디
	 */
	private void validatePermission(Long commentUserId, Long requestUserId, Long commentId) {
		if (!commentUserId.equals(requestUserId)) {
			log.warn("게시글 수정 권한이 없습니다: userId={}, commentId={}", requestUserId, commentId);
			throw new AccessDeniedException(String.format("게시글 수정권한이 없습니다: requestUserId={%d}, commentId={%d}", requestUserId, commentId));
		}
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
			log.debug("contents={}", content);
			throw new RuntimeException("서버 에러 발생");
		}
	}

}
