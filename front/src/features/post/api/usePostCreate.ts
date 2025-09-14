import axiosClient from "../../../shared/axios";
import {useMutation} from "@tanstack/react-query";
import type {PostCreateReq, PostCreateRes} from "../../../entities/post";

const postCreate = async (data: PostCreateReq): Promise<PostCreateRes> => {
  const response = await axiosClient.post<PostCreateRes>('/posts', data);
  return response.data;
}

export const usePostCreate = () => {
  return useMutation<PostCreateRes, Error, PostCreateReq>({
    mutationFn: postCreate,
    onSuccess: (data) => {
      // todo: 게시글 작성 성공

      console.log(data);
    },
    onError: (error) => {
      // todo : 게시글 작성 실패
      console.log(error);
    }
  });
};