package dev.kyudong.back.post.application.port.out.web;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface PostPersistencePort {

	Post findByIdOrThrow(Long postId);

	Post save(Post post);

	boolean existsById(Long postId);

	List<Post> findRecentPostsWithUser(User user, Instant now, int size);

	List<Post> findRecentPostsWithGuest(Instant now, int size);

	List<Post> findPopularPostsWithUser(User user, Instant now, int size);

	List<Post> findPopularPostsWithGuest(Instant now, int size);

	List<Post> findByFollowingPost(User user, Instant now, int size);

	void refreshRandomOldPost();

	List<Post> findAllByIds(Set<Long> postIds);

}
