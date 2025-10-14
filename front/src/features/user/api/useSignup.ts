import { useMutation } from "@tanstack/react-query";
import type { UserCreateReq, UserCreateRes } from "@/entities/user/model/types.ts";
import restClient from "@shared/api/apiClient.ts";

const signup = async (data: UserCreateReq): Promise<UserCreateRes> => {
  const response = await restClient.post<UserCreateRes>('/users', data);
  return  response.data;
};

export const useSignup = () => {
  return useMutation<UserCreateRes, Error, UserCreateReq>({
    mutationFn: signup,
  });
};