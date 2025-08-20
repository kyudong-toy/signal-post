package dev.kyudong.back.file.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Table(name = "FILES")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UPLOADER_ID", nullable = false)
	private User uploader;

	@Column(name = "ORIGINAL_FILE_NAME", nullable = false)
	private String originalFileName;

	@Column(name = "STORED_FILE_NAME", nullable = false)
	private String storedFileName;

	@Column(name = "FILE_PATH", nullable = false)
	private String filePath;

	@Column(name = "FILE_SIZE", nullable = false)
	private Long fileSize;

	@Column(name = "WEB_PATH", nullable = false)
	private String webPath;

	@Column(name = "MIME_TYPE", nullable = false, length = 100)
	private String mimeType;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private FileStatus status;

	@Column(name = "OWNER_ID")
	private Long ownerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "FILE_OWNER_TYPE", length = 20)
	private FileOwnerType fileOwnerType;

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@Column(name = "DELETED_AT")
	private Instant deletedAt;

	@Builder
	private File(User uploader, String originalFileName, String storedFileName, String filePath, Long fileSize, String webPath, String mimeType) {
		this.uploader = uploader;
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.webPath = webPath;
		this.mimeType = mimeType;
		this.status = FileStatus.PENDING;
	}

	public void deleteFile() {
		this.status = FileStatus.DELETED;
	}

	public void activeFile() {
		this.status = FileStatus.ACTIVE;
	}

	public void updateOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public void updateFileOwnerType(FileOwnerType fileOwnerType) {
		this.fileOwnerType = fileOwnerType;
	}

}
