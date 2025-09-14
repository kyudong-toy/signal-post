import {z} from "zod";

export interface ChatRoomEntity {
  lastChatroomId: number,
  lastActivityAt: string,
  hasNext: boolean,
  content: ChatRoomItem[]
}

export interface ChatRoomItem {
  roomId: number,
  memberCount: number,
}

export const useChatRoomCreateSchema = z.object({
  roomname: z.string()
    .trim()
    .min(1, '채팅방 이름은 공백이 될 수 없습니다')
    .max(100, '채팅방 이름은 100자를 초과할 수 없습니다'),
  userIds: z.array(z.number().positive("유효하지 못한 파일입니다"))
});

export type ChatRoomCreateReq = z.infer<typeof useChatRoomCreateSchema>;

export interface ChatRoomCreateRes {
  roomId: number;
  memberCount: number;
  status: 'ACTIVE' | 'DELETED';
  createdAt: string;
}

export interface ChatMessageEntity {
  cursorId: number | null,
  cursorTime: string | null,
  hasNext: boolean,
  content: ChatMessageItem[]
}

export interface ChatMessageItem {
  messageId: number,
  roomId: number,
  senderId: number,
  senderUsername: string,
  content: string,
  messageType: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE',
  messageStatus: 'ACTIVE' | 'DELETED',
  createdAt: string
}

export const useChatMessageCreateSchema = z.object({
  content: z.string()
    .trim()
    .min(1, '메시지를 입력해주세요'),
  messageType: z.string()
});

export type ChatMessageCreateReq = z.infer<typeof useChatMessageCreateSchema>;