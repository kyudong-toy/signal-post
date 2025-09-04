import type {ChatRoomCreateReq, ChatRoomCreateRes} from "@/entities/chat/model/model.ts";
import axiosClient from "@/shared/axios";
import {useMutation} from "@tanstack/react-query";

const roomCreate = async (data: ChatRoomCreateReq): Promise<ChatRoomCreateRes> => {
  console.log('ㅇㅇㅇ');
  const response = await axiosClient.post<ChatRoomCreateRes>('/chatroom', data);
  return response.data;
}

export const useRoomCreate = () => {
  return useMutation<ChatRoomCreateRes, Error, ChatRoomCreateReq>({
    mutationFn: roomCreate,
    onSuccess: (data) => {
      // todo: 채팅방 생성 성공
      console.log(data);
    },
    onError: (error) => {
      // todo : 채팅방 생성 실패
      console.log(error);
    }
  });
};