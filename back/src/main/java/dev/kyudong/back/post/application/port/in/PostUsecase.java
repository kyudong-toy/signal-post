package dev.kyudong.back.post.application.port.in;

import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.entity.Post;

public interface PostUsecase {

	PostDetailResDto findPostById(Long postId);

	PostCreateResDto createPost(Long userId, PostCreateReqDto request);

	PostUpdateResDto updatePost(Long postId, Long userId, PostUpdateReqDto request);

	PostStatusUpdateResDto updatePostStatus(Long postId, Long userId, PostStatusUpdateReqDto request);

	void validatePostExists(Long postId);

	Post getPostEntityOrThrow(Long postId);

}
