import axiosClient from "@shared/axios";
import {  useMutation } from "@tanstack/react-query";
import type {FollowEntity} from "@/entities/follow/model/type.ts";

interface FollowVariable {
  username: string
}

const follow = async (data: FollowVariable): Promise<FollowEntity> => {
  const response = await axiosClient.post(`/users/${data.username}/follow`);
  return response.data;
}

export const useFollow = () => {
  return useMutation({
    mutationFn: follow,
    onSuccess: (data) => {
      console.log('팔로우성공');
      console.log(data);
    },
    onError:  (error) => {
      console.error('팔로우실패')
      console.error(error);
    }
  });
}