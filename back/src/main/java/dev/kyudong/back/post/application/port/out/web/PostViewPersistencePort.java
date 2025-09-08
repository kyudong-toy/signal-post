package dev.kyudong.back.post.application.port.out.web;

import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.user.domain.User;

public interface PostViewPersistencePort {

	/**
	 * 게시글의 조회수를 증가시킵니다, 해당 메서드는 이벤트를 통해 요청됩니다
	 * @param user	조회한 사용자
	 * @param post	조회한 게시글
	 *
	 * @see dev.kyudong.back.post.application.port.in.event.PostViewEventUsecase
	 */
	void increaseViewCount(User user, Post post);

}
