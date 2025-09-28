package dev.kyudong.back.media.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("VIDEO")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoMediaFile extends MediaFile {

	@Column(name = "THUMBNAIL_PATH")
	private String thumbnailPath;

	private VideoMediaFile(User uploader, String originalFileName, Long fileSize, String mimeType) {
		super(uploader, originalFileName, fileSize, mimeType);
	}

	public static VideoMediaFile create(User uploader, String originalFileName, Long fileSize, String mimeType) {
		return new VideoMediaFile(uploader, originalFileName, fileSize, mimeType);
	}

	public void updateThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}

}
