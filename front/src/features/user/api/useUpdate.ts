import axiosClient from "../../../shared/axios";
import {useMutation} from "@tanstack/react-query";
import type {UserUpdateReq, UserUpdateRes} from "@/entities/user/model/types.ts";

const signup = async (data: UserUpdateReq): Promise<UserUpdateRes> => {
  const response = await axiosClient.patch<UserUpdateRes>('/users/me/update', data);
  return  response.data;
};

export const useUpdate = () => {
  return useMutation<UserUpdateRes, Error, UserUpdateReq>({
    mutationFn: signup,
    onSuccess: (data) => {
      // todo: 회원가입 성공
      console.log(data);
    },
    onError: (error) => {
      // todo : 회원가입 실패
      console.log(error);
    }
  });
};