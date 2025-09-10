package dev.kyudong.back.feed.api.dto;

public record ItemWithScore(PostFeedDto detailResDto, double postScore) {
}
