package dev.kyudong.back.user.repository;

import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	@EntityGraph(attributePaths = {"profileImageUrl", "backgroundImageUrl"})
	@Query("""
			SELECT u
			FROM User u
			WHERE u.username = :username
	""")
	Optional<User> findByUsername(@Param("username") String username);

	List<User> findByIdIn(Set<Long> userIds);

}
