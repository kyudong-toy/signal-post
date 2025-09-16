package dev.kyudong.back.user.repository;

import dev.kyudong.back.user.properties.UserTokenProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserTokenRepository {

	private final RedissonClient  redissonClient;
	private final UserTokenProperties userTokenProperties;

	public void saveToken(String username, String refreshToken) {
		String key = userTokenProperties.getRefreshPrefix() + username;
		RBucket<String> bucket = redissonClient.getBucket(key);
		bucket.set(refreshToken, Duration.of(userTokenProperties.getRefreshExpirationTime(), ChronoUnit.MILLIS));
	}

	public String findTokenByUsername(String username) {
		String key = userTokenProperties.getRefreshPrefix() + username;
		RBucket<String> bucket = redissonClient.getBucket(key);
		return bucket.get();
	}

	public void deleteToken(String username) {
		String key = userTokenProperties.getRefreshPrefix() + username;
		RBucket<String> bucket = redissonClient.getBucket(key);
		bucket.deleteAsync();
	}

}
