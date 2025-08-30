package dev.kyudong.back.user.repository;

import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	Optional<User> findByUsername(String username);

	List<User> findByIdIn(Set<Long> userIds);

}
