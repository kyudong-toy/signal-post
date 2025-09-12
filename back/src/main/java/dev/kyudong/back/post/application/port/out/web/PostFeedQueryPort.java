package dev.kyudong.back.post.application.port.out.web;

import dev.kyudong.back.feed.api.dto.PostFeedDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * {@link dev.kyudong.back.feed.domain.Feed}에서 {@link dev.kyudong.back.post.domain.entity.Post} 목록 조회시 사용됩니다.
 */
public interface PostFeedQueryPort {

	List<PostFeedDto> findPreviewPosts(Long userId, int size);

	List<PostFeedDto> findPreviewPosts(int size);

	List<PostFeedDto> findRecentPosts(Long userId, Instant now, int size);

	List<PostFeedDto> findRecentPosts(Instant now, int size);

	List<PostFeedDto> findPopularPosts(Long userId, Instant now, int size);

	List<PostFeedDto> findPopularPosts(Instant now, int size);

	List<PostFeedDto> findByFollowingPost(Long userId, int size);

	List<PostFeedDto> findAllByIds(Long userId, Set<Long> postIds);

	List<PostFeedDto> findAllByIds(Set<Long> postIds);

}
