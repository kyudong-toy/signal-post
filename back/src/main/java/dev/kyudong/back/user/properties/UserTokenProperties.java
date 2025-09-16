package dev.kyudong.back.user.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Slf4j
@Getter
@ConfigurationProperties(prefix = "jwt")
public class UserTokenProperties {

	private final String refreshPrefix;
	private final String accessSecret;
	private final String refreshSecret;
	private final long accessExpirationTime;
	private final long refreshExpirationTime;

	@ConstructorBinding
	public UserTokenProperties(String refreshPrefix, String accessSecret, String refreshSecret, long accessExpirationTime, long refreshExpirationTime) {
		log.debug("JWT 설정을 시작합니다: accessSecret={}, refreshSecret={}", accessSecret, refreshSecret);
		this.refreshPrefix = refreshPrefix;
		this.accessSecret = accessSecret;
		this.refreshSecret = refreshSecret;
		this.accessExpirationTime = accessExpirationTime;
		this.refreshExpirationTime = refreshExpirationTime;
	}

}
