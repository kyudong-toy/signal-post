package dev.kyudong.back.user.security;

import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {

	private final Long id;

	private final String username;

	private final String password;

	private final boolean isEnabled;

	private final Collection<? extends GrantedAuthority> authorities;

	@Builder
	private CustomUserPrincipal(Long id, String username, String password, UserStatus status, UserRole role) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.isEnabled = status.equals(UserStatus.ACTIVE);
		this.authorities = mapRolesToAuthorities(role);
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

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

}
