package dev.kyudong.back.follow.repository;

import dev.kyudong.back.follow.domain.Follow;
import dev.kyudong.back.follow.domain.FollowStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

	boolean existsByFollowerAndFollowing(User follower, User following);

	List<Follow> findByFollowing(User follwing);

	List<Follow> findByFollower(User follower);

	Optional<Follow> findByFollowerAndFollowingAndStatus(User follower, User following, FollowStatus status);

	@Query("""
			SELECT f
			FROM Follow f
			JOIN FETCH f.follower
			WHERE f.following = :user
			AND f.status = 'FOLLOWING'
	""")
	List<Follow> findByFollowingWithFollower(@Param("user") User user);

}
