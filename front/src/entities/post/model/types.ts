import {z} from "zod";
import type {OutputData} from "@editorjs/editorjs";

export interface PostEntity {
  postId: number;
  userId: number;
  subject: string;
  content: string;
  status: 'NORMAL' | 'DELETED';
  createdAt: string;
  modifiedAt: string;
}

export type PostContent = OutputData;

export const postCreateSchema = z.object({
  subject: z.string()
    .trim()
    .min(1, '제목은 공백으로 입력할 수 없습니다')
    .max(100, '제목은 100글자를 초과할 수 없습니다'),
  content: z.object({
    time: z.number().optional(),
    blocks: z.array(z.any()),
    version: z.string().optional()
  }).refine(
    (data) => data.blocks.length > 0,
    '본문은 공백으로 올 수 없습니다'
  ),
  fileIds: z.array(z.number().positive("유효하지 못한 파일입니다"))
});

export type PostCreateReq = z.infer<typeof postCreateSchema>;

export interface PostCreateRes {
  postId: number;
  subject: string;
  content: string;
  createdAt: string;
  modifiedAt: string;
}

export const postUpdateSchema = postCreateSchema.extend({
  delFileIds: z.array(z.number().positive("유효하지 못한 파일입니다"))
});

export type PostUpdateReq = z.infer<typeof postUpdateSchema>;

export interface PostUpdateRes {
  postId: number;
  subject: string;
  content: string;
  createdAt: string;
  modifiedAt: string;
}
