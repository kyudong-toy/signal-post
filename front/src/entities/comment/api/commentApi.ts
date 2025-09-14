import axiosClient from "@/shared/axios";
import type {CommentEntity} from "@/entities/comment";

interface CommentFindVariables {
  postId: number;
  sort: string;
  cursorId?: number;
}

export const findComments = async ({ postId, sort, cursorId }: CommentFindVariables): Promise<CommentEntity> => {
  const response = await axiosClient.get(`/posts/${postId}/comments`, {
    params: {
      sort,
      cursorId,
    }
  });
  return response.data;
}