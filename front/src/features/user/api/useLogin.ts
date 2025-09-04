import axiosClient from "../../../shared/axios";
import {useMutation} from "@tanstack/react-query";
import type {UserLoginReq, UserLoginRes} from "@/entities/user/model/types.ts";
import {useAuthStore} from "@/entities/user/model/authStore.ts";

const login = async (data: UserLoginReq): Promise<UserLoginRes> => {
  const response = await axiosClient.post<UserLoginRes>('/users/login', data);
  return  response.data;
};

export const useLogin = () => {
  const {setAccessToken} = useAuthStore();

  return useMutation<UserLoginRes, Error, UserLoginReq>({
    mutationFn: login,
    onSuccess: (data) => {
      // todo: 로그인 성공
      console.log(data);
      setAccessToken(data.token);
    },
    onError: (error) => {
      // todo : 로그인 실패
      console.log(error);
    }
  });
};