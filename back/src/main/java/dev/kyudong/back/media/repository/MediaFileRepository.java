package dev.kyudong.back.media.repository;

import dev.kyudong.back.media.domain.MediaFile;
import dev.kyudong.back.media.domain.MediaFileOwnerType;
import dev.kyudong.back.media.domain.MediaFileStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

	Optional<MediaFile> findByIdAndUploader(Long id, User uploader);

	List<MediaFile> findByStatusAndCreatedAtBefore(MediaFileStatus status, Instant threshold);

	Optional<MediaFile> findByIdAndOwnerId(Long id, Long OwnerId);

	@Modifying
	@Query("""
		UPDATE MediaFile m
		SET m.status = 'ACTIVE', m.ownerId = :ownerId, m.mediaFileOwnerType = :mediaFileOwnerType
		WHERE m.status = 'PENDING'
		AND m.id = :fileId
	""")
	void confirmFileById(
			@Param("fileIds") Long fileId,
			@Param("ownerId") Long ownerId,
			@Param("mediaFileOwnerType") MediaFileOwnerType mediaFileOwnerType
	);

	@Modifying
	@Query("""
		UPDATE MediaFile m
		SET m.status = 'ACTIVE', m.ownerId = :ownerId, m.mediaFileOwnerType = :mediaFileOwnerType
		WHERE m.status = 'PENDING'
		AND m.id IN :fileIds
	""")
	void confirmFileByIds(
			@Param("fileIds") Set<Long> fileIds,
			@Param("ownerId") Long ownerId,
			@Param("mediaFileOwnerType") MediaFileOwnerType mediaFileOwnerType
	);

	@Modifying
	@Query("""
		UPDATE MediaFile m
		SET m.status = 'DELETED', m.deletedAt = :now
		WHERE m.id IN :delIds
	""")
	void softDeleteByIds(@Param("delIds")Set<Long> delIds, @Param("now") Instant now);

}
