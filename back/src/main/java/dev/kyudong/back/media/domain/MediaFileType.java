package dev.kyudong.back.media.domain;

public enum MediaFileType {

	IMAGE("image"),
	VIDEO("video");

	private final String type;

	MediaFileType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
