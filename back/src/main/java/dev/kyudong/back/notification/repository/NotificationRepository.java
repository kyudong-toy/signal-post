package dev.kyudong.back.notification.repository;

import dev.kyudong.back.notification.domain.Notification;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByCreatedAtBefore(Instant threshold);

	@Query("""
			SELECT n
			FROM Notification n
			JOIN FETCH n.receiver
			WHERE n.receiver = :receiver
			AND n.id < :lastNotificationId
			ORDER BY n.id DESC, n.createdAt DESC
	""")
	Slice<Notification> findNotificationByReceiver(
			@Param("receiver") User receiver,
			@Param("lastNotificationId") Long lastNotificationId,
			Pageable pageable
	);

	@Query("""
			SELECT n
			FROM Notification n
			JOIN FETCH n.receiver
			WHERE n.receiver = :receiver
			ORDER BY n.id DESC, n.createdAt DESC
	""")
	Slice<Notification> findNotificationByReceiver(
			@Param("receiver") User receiver,
			Pageable pageable
	);

	Optional<Notification> findByIdAndReceiver(Long notificationId, User receiver);

	void deleteByIdAndReceiver(Long notificationId, User receiver);

}
