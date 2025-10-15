import { useMutation } from "@tanstack/react-query";
import type { UserLoginReq } from "@/entities/user";
import restClient from "@shared/api/apiClient.ts";
import type { UserValidateRes } from "@/entities/user/model/types.ts";
import { useAuthStore } from "@/entities/user";

const login = async (data: UserLoginReq): Promise<UserValidateRes> => {
  const response = await restClient.post<UserValidateRes>('/auth/login', data);
  return response.data;
};

export const useLogin = () => {
  const { setAccessToken } = useAuthStore();

  return useMutation<UserValidateRes, Error, UserLoginReq>({
    mutationFn: login,
    onSuccess: (data) => {
      if (!data.token) {
        // todo: 에러페이지로 이동
      }
      setAccessToken(data.token);
    },
  });
};