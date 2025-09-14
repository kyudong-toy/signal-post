export interface FollowEntity {
  id: number,
  followerId: number,
  followingId: number,
  status: 'BLOCKED' | 'PENDING' | 'FOLLOWING' | 'UNFOLLOWED'
}