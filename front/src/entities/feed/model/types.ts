export interface FeedEntity {
  lastFeedId: number,
  hasNext: boolean,
  content: FeedItem[]
}

export interface FeedItem {
  postId: number,
  userId: number,
  subject: string,
  content: string,
  status: 'NORMAL' | 'DELETED'
  createdAt: string,
  modifiedAt: string,
}