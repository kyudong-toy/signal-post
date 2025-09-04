import axiosClient from "@/shared/axios";
import type {ChatMessageEntity} from "@/entities/chat/model/model.ts";

interface ChatMessagesFindVariables {
  roomId: number
  cursorId?: number | null,
  cursorTime?: string | null
}

export const findMessages = async ({roomId, cursorId, cursorTime}: ChatMessagesFindVariables): Promise<ChatMessageEntity> => {
  const params: Record<string, string | number> = {};

  if (cursorId) {
    params.cursorId = cursorId;
  }

  if (cursorTime) {
    params.cursorTime = cursorTime;
  }

  const response = await axiosClient.get<ChatMessageEntity>(`/chatroom/${roomId}/message`, {params});
  return response.data;
}