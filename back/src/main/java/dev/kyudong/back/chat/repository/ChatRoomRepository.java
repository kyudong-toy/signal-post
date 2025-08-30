package dev.kyudong.back.chat.repository;

import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.RoomStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@Query("""
			SELECT c
			FROM ChatRoom c
			JOIN FETCH c.chatMembers cm
			WHERE cm.user = :user
			AND c.status = 'ACTIVE'
			AND (c.lastActivityAt < :cursorTime OR (c.lastActivityAt = :cursorTime AND c.id < :lastChatroomId))
			ORDER BY c.lastActivityAt DESC, c.id DESC
	""")
	Slice<ChatRoom> findChatRooomsByMember(
			@Param("user") User user,
			@Param("lastChatroomId") Long lastChatroomId,
			@Param("cursorTime") Instant cursorTime,
			PageRequest pageRequest
	);

	@Query("""
			SELECT c
			FROM ChatRoom c
			JOIN FETCH c.chatMembers cm
			WHERE cm.user = :user
			AND c.status = 'ACTIVE'
			ORDER BY c.lastActivityAt DESC, c.id DESC
	""")
	Slice<ChatRoom> findChatRooomsByMember(
			@Param("user") User user,
			PageRequest pageRequest
	);

	@Modifying
	@Query("""
			UPDATE ChatRoom c
			SET c.status = 'DELETED'
			WHERE c.id IN :chatRoomIds
	""")
	void updateOrphanedChatroom(@Param("chatRoomIds") List<Long> chatRoomIds);

	Optional<ChatRoom> findChatroomByIdAndStatus(Long chatroomId, RoomStatus status);

	@Query("""
			SELECT c.id
			FROM ChatRoom c
			JOIN FETCH ChatMember cm ON cm.chatRoom = c
			WHERE cm.status != 'JOINED'
			AND c.createdAt < :threshold
	""")
	List<Long> findByOrphanedChatroomIds(@Param("threshold")Instant threshold);

}
