package dev.kyudong.back.post.application.port.in.web;

import dev.kyudong.back.post.domain.dto.web.req.CommentCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.CommentUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.*;
import dev.kyudong.back.post.domain.entity.CommentSort;

public interface CommentUsecase {

	CommentListResDto findComments(Long postId, Long cursorId, CommentSort sort);

	CommentCreateResDto createComment(Long postId, Long userId, CommentCreateReqDto request);

	CommentUpdateResDto updateComment(Long postId, Long commentId, Long userId, CommentUpdateReqDto request);

	CommentStatusUpdateResDto updateCommentStatus(Long postId, Long commentId, Long userId, CommentStatusUpdateReqDto request);

}
