package dev.kyudong.back.notification.event;

import dev.kyudong.back.post.domain.dto.event.PostCreateNotification;

public interface NotificationEventHandler {

	void handlePostCreateEvent(PostCreateNotification event);

}
