import {z} from "zod";

export const useSignupSchema = z.object({
  username: z.string()
    .trim()
    .min(4, '아이디는 4자 이상이어야 합니다')
    .max(30, '아이디는 30자를 초과할 수 없습니다'),
  password: z.string()
    .trim()
    .min(4, '비밀번호는 최소 4자 이상이어야 합니다')
    .max(150, '비밀번호는 150자를 초과할 수 없습니다'),
  displayName: z.string()
    .trim()
    .min(3, '사용자 이름은 최소 3자 이상이어야 합니다')
    .max(20, '사용자 이름은 20자를 초과할 수 없습니다')
});

export type UserCreateReq = z.infer<typeof useSignupSchema>;

export interface UserCreateRes {
  id: number;
  username: string;
  status: 'ACTIVE' | 'DORMANT' | 'DELETED';
  role: 'USER' | 'ADMIN';
}

export const useLoginSchema = z.object({
  username: z.string()
    .trim()
    .min(4, '아이디는 4자 이상이어야 합니다')
    .max(30, '아이디는 30자를 초과할 수 없습니다'),
  password: z.string()
    .trim()
    .min(4, '비밀번호는 최소 4자 이상이어야 합니다')
    .max(150, '비밀번호는 150자를 초과할 수 없습니다')
});

export type UserLoginReq = z.infer<typeof useLoginSchema>;

export interface UserLoginRes {
  id: number;
  username: string;
  token: string;
}

export const useUpdatechema = z.object({
  password: z.string()
    .trim()
    .min(4, '비밀번호는 최소 4자 이상이어야 합니다')
    .max(150, '비밀번호는 150자를 초과할 수 없습니다')
});

export type UserUpdateReq = z.infer<typeof useUpdatechema>;

export interface UserUpdateRes {
  id: number;
  username: string;
  status: 'ACTIVE' | 'DORMANT' | 'DELETED';
}