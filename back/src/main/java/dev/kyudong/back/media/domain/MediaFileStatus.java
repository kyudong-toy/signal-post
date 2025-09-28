package dev.kyudong.back.media.domain;

public enum MediaFileStatus {

	ACTIVE,				 // 정상
	DELETED,			 // 삭제
	PENDING,    		 // 대기
	PROCESSING,     	 // 진행중
	FAILED,     		 // 작업 실패

}
