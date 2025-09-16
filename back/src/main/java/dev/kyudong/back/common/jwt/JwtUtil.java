package dev.kyudong.back.common.jwt;

import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.exception.UserTokenExpiredExcpetion;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUtil {

	private final Key accessKey;
	private final Key refreshKey;
	private final long accessTokenExpirationTime;
	private final long refreshTokenExpirationTime;

	public JwtUtil(
			final String accessSecret,
			final String refreshSecret,
			long accessTokenValidityInMilliseconds,
			long refreshTokenValidityInMilliseconds
	) {
		byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
		this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);

		byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecret);
		this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);

		this.accessTokenExpirationTime = accessTokenValidityInMilliseconds;
		this.refreshTokenExpirationTime = refreshTokenValidityInMilliseconds;
	}

	public String createAccessToken(User user) {
		return generateToken(user, accessKey, accessTokenExpirationTime);
	}

	public String createRefreshToken(User user) {
		return generateToken(user, refreshKey, refreshTokenExpirationTime);
	}

	private String generateToken(User user, Key key, long expirationTime) {
		log.debug("토큰 생성을 시작합니다: userId={}, username={}", user.getId(), user.getUsername());
		Map<String, Object> header = new HashMap<>();
		header.put(JwsHeader.TYPE, JwsHeader.JWT_TYPE);
		header.put(JwsHeader.ALGORITHM, SignatureAlgorithm.HS256);

		Claims claims = Jwts.claims().setSubject(user.getUsername());
		claims.put("id", user.getId());
		claims.put("role", user.getRole());
		claims.put("status", user.getStatus());

		Date now = new Date();
		Date expiredTime = new Date(now.getTime() + expirationTime);
		String token = Jwts.builder()
				.setHeader(header)
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(expiredTime)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();

		log.debug("토큰을 생성했습니다: userId={}, username={}, time={}", user.getId(), user.getUsername(), LocalDateTime.now());
		return token;
	}

	public Claims getClaimsFromAccessToken(String token) {
		try {
			return Jwts.parserBuilder()
					.setSigningKey(accessKey)
					.build()
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException ex) {
			throw new UserTokenExpiredExcpetion();
		} catch (SignatureException s) {
			throw new RuntimeException();
		}
	}

	public boolean validateRefreshToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(refreshKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
			return true;
		} catch (ExpiredJwtException ex) {
			log.warn("만료된 토큰입니다");
			return false;
		} catch (SignatureException s) {
			log.error("유효하지 않는 암호화입니다");
			return false;
		} catch (Exception e) {
			log.error("토큰 파싱중 에러 발생: token={}", token);
			return false;
		}
	}

}