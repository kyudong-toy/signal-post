package dev.kyudong.back.common.config;

import dev.kyudong.back.common.jwt.JwtUtil;
import dev.kyudong.back.user.properties.UserTokenProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtUtilConfig {

	@Bean
	public JwtUtil jwtUtil(UserTokenProperties userTokenProperties) {
		return new JwtUtil(
				userTokenProperties.getAccessSecret(),
				userTokenProperties.getRefreshSecret(),
				userTokenProperties.getAccessExpirationTime(),
				userTokenProperties.getRefreshExpirationTime()
		);
	}

}
