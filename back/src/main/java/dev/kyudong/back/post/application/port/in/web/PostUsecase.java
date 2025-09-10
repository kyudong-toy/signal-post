package dev.kyudong.back.post.application.port.in.web;

import dev.kyudong.back.post.domain.dto.web.req.PostCreateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostStatusUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.req.PostUpdateReqDto;
import dev.kyudong.back.post.domain.dto.web.res.PostCreateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostDetailResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostStatusUpdateResDto;
import dev.kyudong.back.post.domain.dto.web.res.PostUpdateResDto;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface PostUsecase {

	PostDetailResDto findPostByIdWithUser(Long userId, Long postId);

	PostDetailResDto findPostByIdWithGuest(String guestId, Long postId);

	PostCreateResDto createPost(Long userId, PostCreateReqDto request);

	PostUpdateResDto updatePost(Long postId, Long userId, PostUpdateReqDto request);

	PostStatusUpdateResDto updatePostStatus(Long postId, Long userId, PostStatusUpdateReqDto request);

	void validatePostExists(Long postId);

	Post getPostEntityOrThrow(Long postId);

	void refreshRandomOldPost();

}
