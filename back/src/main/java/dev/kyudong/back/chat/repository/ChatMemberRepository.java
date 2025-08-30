package dev.kyudong.back.chat.repository;

import dev.kyudong.back.chat.domain.ChatMember;
import dev.kyudong.back.chat.domain.ChatRoom;
import dev.kyudong.back.chat.domain.MemberStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

	@Query("""
			SELECT cm.user.id
			FROM ChatMember cm
			WHERE cm.chatRoom = :chatRoom
			AND cm.user.id IN :userIds
			AND cm.status = :status
	""")
	Set<Long> findExistsMemberUserIds(
			@Param("userIds") Set<Long> userIds,
			@Param("chatRoom") ChatRoom chatRoom,
			@Param("status") MemberStatus status
	);

	Optional<ChatMember> findByUserAndChatRoom(User leaveUser, ChatRoom chatRoom);

}
