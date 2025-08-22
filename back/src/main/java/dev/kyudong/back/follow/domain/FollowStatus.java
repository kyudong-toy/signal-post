package dev.kyudong.back.follow.domain;

public enum FollowStatus {

	BLOCKED,		// 차단당함
	PENDING,		// 팔로우 승인 대기
	FOLLOWING,		// 팔로우 중
	UNFOLLOWED 	    // 삭제한 팔로우

}
