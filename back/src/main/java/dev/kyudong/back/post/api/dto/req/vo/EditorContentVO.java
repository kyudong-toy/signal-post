package dev.kyudong.back.post.api.dto.req.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 블록 에디터 전체 데이터입니다
 * Editor.js와 연동됩니다
 * @param time		작성시간
 * @param blocks	에디터 본문
 * @param version   에디터 버전
 */
public record EditorContentVO(
		@NotNull Long time,
		@NotEmpty List<EditorBlockVO> blocks,
		@NotBlank String version
) {
}
