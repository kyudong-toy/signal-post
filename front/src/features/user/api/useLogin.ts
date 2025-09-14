import axiosClient from "../../../shared/axios";
import { useMutation } from "@tanstack/react-query";
import type { UserLoginReq, UserLoginRes } from "@/entities/user";
import { useAuth } from "@/entities/user/hooks/useAuth.ts";

const login = async (data: UserLoginReq): Promise<UserLoginRes> => {
  const response = await axiosClient.post<UserLoginRes>('/users/login', data);
  return response.data;
};

export const useLogin = () => {
  const { setAuth } = useAuth();

  return useMutation<UserLoginRes, Error, UserLoginReq>({
    mutationFn: login,
    onSuccess: (data) => {
      setAuth(data);
    },
    onError: (error) => {
      console.debug('로그인에 실패했습니다 : ' + error)
    }
  });
};