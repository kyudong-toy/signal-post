package dev.kyudong.back.media.domain;

import dev.kyudong.back.user.domain.User;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageMediaFile extends MediaFile {

	private ImageMediaFile(User uploader, String originalFileName, Long fileSize, String mimeType) {
		super(uploader, originalFileName, fileSize, mimeType);
	}

	public static ImageMediaFile create(User uploader, String originalFileName, Long fileSize, String mimeType) {
		return new ImageMediaFile(uploader, originalFileName, fileSize, mimeType);
	}

}
