package dev.kyudong.back.feed.event;

import dev.kyudong.back.post.api.dto.event.PostCreateFeedEvent;

public interface FeedEventHandler {

	/**
	 * 게시글 생성 후 피드(Feed)에 게시글 생성 소식을 추가합니다.
	 * @param event 게시글 관련 정보(사용자. 게시글)
	 */
	void handlePostCreate(PostCreateFeedEvent event);

}
