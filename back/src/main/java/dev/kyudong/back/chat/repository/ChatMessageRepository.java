package dev.kyudong.back.chat.repository;

import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatMessage;
import dev.kyudong.back.chat.domain.ChatRoom;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	Optional<ChatMessage> findByIdAndChatRoomAndSender(Long messageId, ChatRoom chatRoom, ChatMember sender);

	@Query("""
			SELECT cm
			FROM ChatMessage cm
			WHERE cm.chatRoom.id = :roomId
			ORDER BY cm.createdAt DESC, cm.id DESC
	""")
	Slice<ChatMessage> findChatMessage(@Param("roomId") Long roomId, PageRequest pageRequest);

	@Query("""
			SELECT cm
			FROM ChatMessage cm
			WHERE cm.chatRoom.id = :roomId
			AND (cm.createdAt < :cursorTime OR (cm.createdAt = :cursorTime AND cm.id = :cursorId))
			ORDER BY cm.createdAt DESC, cm.id DESC
	""")
	Slice<ChatMessage> findChatMessageByCursorTime(
			@Param("roomId") Long roomId,
			@Param("cursorId") Long cursorId,
			@Param("cursorTime") Instant cursorTime,
			PageRequest pageRequest
	);

}
