package dev.kyudong.back.post.api.dto.req.vo;

/**
 * 블록 에디터 본문입니다
 * @see EditorContentVO
 * @param id		블록 아이디
 * @param type		타입
 * @param data 		데이터
 */
public record EditorBlockVO(String id, String type, Object data) {
}
