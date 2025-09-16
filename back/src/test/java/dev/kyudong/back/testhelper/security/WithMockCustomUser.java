package dev.kyudong.back.testhelper.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.kyudong.back.user.domain.UserRole;
import dev.kyudong.back.user.domain.UserStatus;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * 컨트롤러 테스트를 위해 사용됩니다
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

	long id() default 1L;

	String username() default "testUser";

	UserStatus status() default UserStatus.ACTIVE;

	UserRole role() default UserRole.USER;

	String password() default "password";

}