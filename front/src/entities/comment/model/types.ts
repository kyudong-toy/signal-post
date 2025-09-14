import {z} from "zod";

export interface CommentEntity {
  hasNext: boolean,
  cursorId: number,
  comments: CommentItem[]
}

export interface CommentItem {
  author: {
    id: number,
    username: string
  },
  content: {
    id: number,
    content: string,
    status: 'NORMAL' | 'DELETED',
    createdAt: string,
  }
}

export const commentRquestSchema = z.object({
  content: z.any()
});

export type CommentCreateReq = z.infer<typeof commentRquestSchema>;

export interface CommentCreateRes  extends CommentEntity {}

export type CommentUpdateReq = z.infer<typeof commentRquestSchema>;

export interface CommentUpdateRes extends CommentEntity {}