package dev.kyudong.back.media.api.dto;

import dev.kyudong.back.media.api.dto.req.MediaFileUploadStartReqDto;
import dev.kyudong.back.media.domain.MediaFileType;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 파일 업로드시 기록용으로 사용됩니다
 */
@Getter
@ToString
public class MediaFileTransaction implements Serializable {
	private final Long uploaderId;
	private final String fileName;
	private final String mimeType;
	private final long fileSize;
	private final MediaFileType type;
	private final int totalChunkCount;
	private final Set<Integer> receivedChunks;

	private MediaFileTransaction(Long uploaderId, String fileName, String mimeType, long fileSize, int totalChunkCount, MediaFileType type) {
		this.uploaderId = uploaderId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.fileSize = fileSize;
		this.type = type;
		this.totalChunkCount = totalChunkCount;
		this.receivedChunks = new HashSet<>();
	}

	public static MediaFileTransaction of(Long uploaderId, MediaFileUploadStartReqDto request) {
		return new MediaFileTransaction(uploaderId, request.fileName(), request.mimeType(), request.fileSize(), request.totalChunkCount(), request.type());
	}

	public void addChunk(int chunkNumber) {
		this.receivedChunks.add(chunkNumber);
	}

	public boolean hasChunk(int chunkNumber) {
		return this.receivedChunks.contains(chunkNumber);
	}

	public boolean isComplete() {
		return this.receivedChunks.size() == this.totalChunkCount;
	}
}
