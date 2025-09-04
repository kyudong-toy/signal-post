import axiosClient from "@/shared/axios";
import {useMutation} from "@tanstack/react-query";
import type {ChatMessageCreateReq} from "@/entities/chat/model/model.ts";

interface ChatMessageCreateVariables {
  roomId: number;
  data: ChatMessageCreateReq
}

const chatMessageCreate = async ({roomId, data}: ChatMessageCreateVariables): Promise<void> => {
  console.log('ㅇㅇㅁㅇㄹㅋㄴㅇㄹ');
  await axiosClient.post<void>(`/chatroom/${roomId}/message`, data);
}

export const useChatMessageCreate = () => {
  return useMutation<void, Error, ChatMessageCreateVariables>({
    mutationFn: chatMessageCreate,
    onSuccess: (data, variables) => {
      // todo: 메시지 전송 성공
      console.log(`data : ${data}`);
      console.log(`variables : ${variables}`);
    },
    onError: (error) => {
      // todo: 댓글 전송 실패
      console.log(error);
    }
  });
}