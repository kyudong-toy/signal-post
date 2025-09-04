import type {CommentCreateReq, CommentCreateRes} from "@/entities/comment/model/types.ts";
import axiosClient from "@/shared/axios";
import {useMutation} from "@tanstack/react-query";

interface CommentCreateVariables {
  postId: number;
  data: CommentCreateReq
}

const commentCreate = async ({postId, data}: CommentCreateVariables): Promise<CommentCreateRes> => {
  const response = await axiosClient.post<CommentCreateRes>(`/posts/${postId}/comments`, data);
  return response.data;
}

export const useCommentCreate = () => {
  return useMutation<CommentCreateRes, Error, CommentCreateVariables>({
    mutationFn: commentCreate,
    onSuccess: (data, variables) => {
      // todo: 댓글 작성 성공
      console.log(`data : ${data}`);
      console.log(`variables : ${variables}`);
    },

    onError: (error) => {
      // todo: 댓글 작성 실패
      console.log(error);
    }
  });
}