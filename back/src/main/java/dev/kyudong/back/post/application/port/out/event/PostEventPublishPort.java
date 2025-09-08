package dev.kyudong.back.post.application.port.out.event;

import dev.kyudong.back.post.domain.entity.Post;

import java.util.Set;

public interface PostEventPublishPort {

	void postCreateEventPublish(Post post, Set<Long> fileIds);

	void postUpdateEventPublish(Post post, Set<Long> fileIds);

}
