package dev.kyudong.back.interaction.repository;

import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

	boolean existsByUserAndTargetId(User user, Long targetId);

	Optional<Interaction> findByUserAndTargetIdAndTargetType(User user, Long targetId, TargetType targetType);

}
