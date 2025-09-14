export interface NotificationEntity {
  hasNext: boolean,
  cursorId: number | null,
  contents: NotificationItem[]
}

export interface NotificationItem {
  sender: NotificationSender
  content: NotificationContent
}

export interface NotificationSender {
  senderId: number,
  senderName: string
}

export const NOTIFICATION_TYPE = {
  POST: 'POST',
  FOLLOW: 'FOLLOW',
  COMMENT: 'COMMENT',
} as const;

type NotificationType = typeof NOTIFICATION_TYPE[keyof typeof NOTIFICATION_TYPE];
export interface NotificationContent {
  id: number,
  receiverId: number,
  redirectUrl: string,
  type: NotificationType,
  createdAt: string
  cotext: {
    postId: number | null,
    postSubject: string | null,
    postSummary: string | null,
    commentId: number | null,
    commentContent: string | null
  }
}