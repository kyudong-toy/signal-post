import type {CommentUpdateReq, CommentUpdateRes} from "@/entities/comment/model/types.ts";
import axiosClient from "@/shared/axios";
import {useMutation} from "@tanstack/react-query";

interface CommentUpdateVariables {
  postId: number;
  commentId: number;
  data: CommentUpdateReq
}

const commentUpdate = async ({postId, commentId, data}: CommentUpdateVariables): Promise<CommentUpdateRes> => {
  const response = await axiosClient.patch<CommentUpdateRes>(`/posts/${postId}/comments/${commentId}`, data);
  return response.data;
}

export const useCommentUpdate = () => {
  return useMutation<CommentUpdateRes, Error, CommentUpdateVariables>({
    mutationFn: commentUpdate,
    onSuccess: (data, variables) => {
      // todo: 댓글 수정 성공
      console.log(`data : ${data}`);
      console.log(`variables : ${variables}`);
    },

    onError: (error) => {
      // todo: 댓글 수정 실패
      console.log(error);
    }
  });
}