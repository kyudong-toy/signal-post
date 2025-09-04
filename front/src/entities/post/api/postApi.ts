import axiosClient from "@/shared/axios";
import type {PostEntity} from "@/entities/post/model/types.ts";

export const findPostById = async (postId: number): Promise<PostEntity> => {
  const response = await axiosClient.get<PostEntity>(`/posts/${postId}`);
  return response.data;
}