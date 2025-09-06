package dev.kyudong.back.post.application.port.in;

import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.CommentUpdateResDto;

import java.util.List;

public interface CommentUsecase {

	List<CommentDetailResDto> findCommentsByPostId(Long postId);

	CommentCreateResDto createComment(Long postId, Long userId, CommentCreateReqDto request);

	CommentUpdateResDto updateComment(Long postId, Long commentId, Long userId, CommentUpdateReqDto request);

	CommentStatusUpdateResDto updateCommentStatus(Long postId, Long commentId, Long userId, CommentStatusUpdateReqDto request);

}
