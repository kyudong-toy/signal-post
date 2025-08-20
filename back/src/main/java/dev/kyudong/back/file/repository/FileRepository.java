package dev.kyudong.back.file.repository;

import dev.kyudong.back.file.domain.File;
import dev.kyudong.back.file.domain.FileStatus;
import dev.kyudong.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

	Optional<File> findByIdAndUploader(Long id, User uploader);

	List<File> findByStatusAndCreatedAtBefore(FileStatus status, Instant threshold);

}
