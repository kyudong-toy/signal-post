package dev.kyudong.back.feed.repository;

import dev.kyudong.back.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {
}
