package dev.kyudong.back.file.repository;

import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileOwnerType;
import dev.kyudong.back.file.domain.FileStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FileRepository extends JpaRepository<File, Long> {

	Optional<File> findByIdAndUploader(Long id, User uploader);

	List<File> findByStatusAndCreatedAtBefore(FileStatus status, Instant threshold);

	Optional<File> findByIdAndOwnerId(Long id, Long OwnerId);

	@Modifying
	@Query("""
		UPDATE File f
		SET f.status = 'ACTIVE', f.ownerId = :ownerId, f.fileOwnerType = :fileOwnerType
		WHERE f.status = 'PENDING'
		AND f.id = :fileId
	""")
	void confirmFileById(
			@Param("fileIds") Long fileId,
			@Param("ownerId") Long ownerId,
			@Param("fileOwnerType") FileOwnerType fileOwnerType
	);

	@Modifying
	@Query("""
		UPDATE File f
		SET f.status = 'ACTIVE', f.ownerId = :ownerId, f.fileOwnerType = :fileOwnerType
		WHERE f.status = 'PENDING'
		AND f.id IN :fileIds
	""")
	void confirmFileByIds(
			@Param("fileIds") Set<Long> fileIds,
			@Param("ownerId") Long ownerId,
			@Param("fileOwnerType") FileOwnerType fileOwnerType
	);

	@Modifying
	@Query("""
		UPDATE File f
		SET f.status = 'DELETED', f.deletedAt = :now
		WHERE f.id IN :delIds
	""")
	void softDeleteByIds(@Param("delIds")Set<Long> delIds, @Param("now") Instant now);

}
