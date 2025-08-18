package dev.kyudong.back.common.jwt;

import dev.kyudong.back.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

	private final Key key;
	private final long accessTokenExpirationTime;

	public JwtUtil(
			@Value("${jwt.secret}") String secretKey,
			@Value("${jwt.expiration-time}") long accessTokenValidityInMilliseconds
	) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.accessTokenExpirationTime = accessTokenValidityInMilliseconds;
	}

	public String generateToken(User user) {
		log.debug("토큰 생성을 시작합니다. userId: {}, username: {}", user.getId(), user.getUsername());
		Map<String, Object> header = new HashMap<>();
		header.put(JwsHeader.TYPE, JwsHeader.JWT_TYPE);
		header.put(JwsHeader.ALGORITHM, SignatureAlgorithm.HS256);

		Claims claims = Jwts.claims().setSubject(user.getUsername());
		claims.put("id", user.getId());
		claims.put("role", user.getRole());
		claims.put("status", user.getStatus());

		Date now = new Date();
		Date expiredTime = new Date(now.getTime() + this.accessTokenExpirationTime);
		String token =  Jwts.builder()
							.setHeader(header)
							.setClaims(claims)
							.setIssuedAt(now)
							.setExpiration(expiredTime)
							.signWith(key, SignatureAlgorithm.HS256)
							.compact();

		log.info("토큰을 생성 했습니다. userId: {}, username: {}, time: {}", user.getId(), user.getUsername(), LocalDateTime.now());
		return token;
	}

	public String getUsernameFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = getClaims(token);
			return !claims.getExpiration().before(new Date());
		} catch (Exception e) {
			log.warn("토큰 검증에 실패했습니다. token: {}", token);
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

}
