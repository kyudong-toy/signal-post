import type {PostUpdateReq, PostUpdateRes} from "@/entities/post/model/types.ts";
import axiosClient from "../../../shared/axios";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {postQueryKeys} from "@/entities/post/api/usePostQuery.ts";

interface PostUpdateVariables {
  postId: number;
  data: PostUpdateReq;
}

const postUpdate = async ({postId, data}: PostUpdateVariables): Promise<PostUpdateRes> => {
  const response = await axiosClient.patch<PostUpdateRes>(`/posts/${postId}/update`, data);
  return response.data;
}

export const usePostUpdate = () => {
  return useMutation<PostUpdateRes, Error, PostUpdateVariables>({
    mutationFn: postUpdate,
    onSuccess: (data, variables) => {
      // todo: 게시글 작성 성공
      console.log(data);

      // 캐시 갱신
      useQueryClient()
        .invalidateQueries({queryKey: postQueryKeys.detail(variables.postId)})
    },
    onError: (error) => {
      // todo : 게시글 작성 실패
      console.log(error);
    }
  });
};