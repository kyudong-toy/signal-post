import axiosClient from "@/shared/axios";

export const findCommentsByPostId = async (postId: number): Promise<Comment[]> => {
  const response = await axiosClient.get<Comment[]>(`/posts/${postId}/comments`);
  return response.data;
}