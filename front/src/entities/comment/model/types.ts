import {z} from "zod";

export interface CommentEntity {
  commentId: number;
  postId: number;
  userId: number;
  content: string;
  status: 'NORMAL' | 'DELETED';
  createdAt: string;
  modifiedAt: string;
}

export const commentCreateSchema = z.object({
  content: z.string()
    .min(1, '댓글 본문은 공백으로 올 수 없습니다')
});

export type CommentCreateReq = z.infer<typeof commentCreateSchema>;

export interface CommentCreateRes  extends CommentEntity {}

export const commentUpdateSchema = z.object({
  content: z.string()
    .min(1, '댓글 본문은 공백으로 올 수 없습니다')
});

export type CommentUpdateReq = z.infer<typeof commentUpdateSchema>;

export interface CommentUpdateRes extends CommentEntity {}