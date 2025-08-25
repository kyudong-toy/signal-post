package dev.kyudong.back.notification.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RedirectUrlCreator {

	public String createPostUrl(Long postId) {
		return UriComponentsBuilder.newInstance()
				.path("/post/{postId}")
				.buildAndExpand(postId)
				.toUriString();
	}

}
