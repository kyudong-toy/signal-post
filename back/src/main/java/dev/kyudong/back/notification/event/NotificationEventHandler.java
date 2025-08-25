package dev.kyudong.back.notification.event;

import dev.kyudong.back.post.api.dto.event.PostCreateNotification;

public interface NotificationEventHandler {

	void handlePostCreateEvent(PostCreateNotification event);

}
