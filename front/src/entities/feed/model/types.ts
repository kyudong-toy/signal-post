export interface FeedEntity {
  hasNext: boolean,
  nextPage: number,
  content: FeedItem[]
}

export interface FeedItem {
  author: {
    id: number,
    username: string
  },
  content: {
    postId: number,
    subject: string,
    content: string,
    viewCount: number,
    commentCount: number,
    status: 'NORMAL' | 'DELETED',
    createdAt: string,
    modifiedAt: string
  }
}