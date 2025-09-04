import axiosClient from "../../../shared/axios";
import {useMutation} from "@tanstack/react-query";
import type {UserCreateReq, UserCreateRes} from "@/entities/user/model/types.ts";

const signup = async (data: UserCreateReq): Promise<UserCreateRes> => {
  const response = await axiosClient.post<UserCreateRes>('/users', data);
  return  response.data;
};

export const useSignup = () => {
  return useMutation<UserCreateRes, Error, UserCreateReq>({
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