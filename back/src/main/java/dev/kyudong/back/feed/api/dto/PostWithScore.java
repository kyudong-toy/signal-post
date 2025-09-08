package dev.kyudong.back.feed.api.dto;

import dev.kyudong.back.post.domain.entity.Post;

public record PostWithScore(Post post, double postScore) {
}
