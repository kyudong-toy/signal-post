import {z} from "zod";

export interface PostEntity {
  postId: number;
  userId: number;
  subject: string;
  content: string;
  status: 'NORMAL' | 'DELETED';
  createdAt: string;
  modifiedAt: string;
}

export const postRequestSchema = z.object({
  subject: z.string()
    .trim()
    .min(1, '제목은 공백으로 입력할 수 없습니다')
    .max(100, '제목은 100글자를 초과할 수 없습니다'),
  content: z.any(),
  fileIds: z.array(z.number().positive("유효하지 못한 파일입니다")),
  tags: z.array(z.string().min(1, "잘못된 태그입니다"))
});

export type PostCreateReq = z.infer<typeof postRequestSchema>;

export interface PostCreateRes {
  postId: number;
  subject: string;
  content: string;
  createdAt: string;
  modifiedAt: string;
}

export type PostUpdateReq = z.infer<typeof postRequestSchema>;

export interface PostUpdateRes {
  postId: number;
  subject: string;
  content: string;
  createdAt: string;
  modifiedAt: string;
}
