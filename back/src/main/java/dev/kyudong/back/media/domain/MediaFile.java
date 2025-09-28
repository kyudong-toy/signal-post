package dev.kyudong.back.media.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Table(name = "MEDIA_FILES")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "MEDUA_FILE_TYPE", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MediaFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UPLOADER_ID", nullable = false)
	private User uploader;

	@Column(name = "ORIGINAL_FILE_NAME", nullable = false)
	private String originalFileName;

	@Column(name = "STORED_FILE_NAME")
	private String storedFileName;

	@Column(name = "FILE_PATH")
	private String filePath;

	@Column(name = "FILE_SIZE", nullable = false)
	private Long fileSize;

	@Column(name = "WEB_PATH")
	private String webPath;

	@Column(name = "MIME_TYPE", nullable = false, length = 100)
	private String mimeType;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	private MediaFileStatus status;

	@Column(name = "OWNER_ID")
	private Long ownerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "FILE_OWNER_TYPE", length = 20)
	private MediaFileOwnerType mediaFileOwnerType;

	@CreatedDate
	@Column(name = "STARTED_AT", updatable = false)
	private Instant startedAt;

	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@Column(name = "DELETED_AT")
	private Instant deletedAt;

	protected MediaFile(User uploader, String originalFileName, Long fileSize, String mimeType) {
		this.uploader = uploader;
		this.originalFileName = originalFileName;
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.status = MediaFileStatus.PROCESSING;
	}

	public void pending() {
		this.status = MediaFileStatus.PENDING;
	}

	public void delete() {
		this.status = MediaFileStatus.DELETED;
	}

	public void active() {
		this.status = MediaFileStatus.ACTIVE;
	}

	public void processingFail() {
		this.status = MediaFileStatus.FAILED;
	}

	public void updateStoredFileName(String storedFileName) {
		this.storedFileName = storedFileName;
	}

	public void updateWebPath(String webPath) {
		this.webPath = webPath;
	}

	public void updateOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public void updateFileOwnerType(MediaFileOwnerType mediaFileOwnerType) {
		this.mediaFileOwnerType = mediaFileOwnerType;
	}

	public void updateFilePath(String filePath) {
		this.filePath = filePath;
	}

}
