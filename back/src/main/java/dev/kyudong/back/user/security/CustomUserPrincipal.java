package dev.kyudong.back.user.security;

import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails, Principal, Serializable {

	private final Long id;

	private final String username;

	private final boolean isEnabled;

	private final Collection<? extends GrantedAuthority> authorities;

	@Builder
	private CustomUserPrincipal(Long id, String username, UserStatus status, UserRole role) {
		this.id = id;
		this.username = username;
		this.isEnabled = status.equals(UserStatus.ACTIVE);
		this.authorities = mapRolesToAuthorities(role);
	}

	public static CustomUserPrincipal createPrincipalFromClaims(Claims claims) {
		String username = claims.getSubject();
		Long id = claims.get("id", Long.class);

		String roleString = claims.get("role", String.class);
		UserRole role = UserRole.valueOf(roleString);

		String statusString = claims.get("status", String.class);
		UserStatus status = UserStatus.valueOf(statusString);

		return new CustomUserPrincipal(id, username, status, role);
	}

	/**
	 * UserRole Enum을 Spring Security의 GrantedAuthority 컬렉션으로 변환합니다.
	 * Spring Security는 역할(Role) 이름 앞에 "ROLE_" 접두사가 붙는 것을 기본으로 합니다.
	 * @param role 사용자의 역할
	 * @return Spring Security가 이해할 수 있는 권한 목록
	 */
	private Collection<? extends GrantedAuthority> mapRolesToAuthorities(UserRole role) {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	public Long getId() {
		return this.id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	/**
	 * 인증 객체에서는 비밀번호를 사용하지 않습니다
	 * @return null
	 */
	@Deprecated
	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public String getName() {
		return this.username;
	}

}
