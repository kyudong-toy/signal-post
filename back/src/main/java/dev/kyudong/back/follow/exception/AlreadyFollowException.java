package dev.kyudong.back.follow.exception;

public class AlreadyFollowException extends RuntimeException {
	public AlreadyFollowException(Long followerId, String followingUsername) {
		super(String.format("이미 팔로잉 중 입니다: followerId={%d}, followingUsername={%s}", followerId, followingUsername));
	}
}
