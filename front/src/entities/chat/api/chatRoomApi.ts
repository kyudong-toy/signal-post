import axiosClient from "@/shared/axios";
import type {ChatRoomEntity} from "@/entities/chat/model/model.ts";
import dayjs from "dayjs";

interface ChatRoomFindVariables {
  lastChatroomId?: number | null,
  cursorTime?: string | null
}

export const findChatRooms = async ({lastChatroomId, cursorTime}: ChatRoomFindVariables): Promise<ChatRoomEntity> => {
  const finalCursorTime = cursorTime || dayjs().toISOString();

  const params: Record<string, string> = {
    cursorTime: finalCursorTime,
  };

  if (lastChatroomId) {
    params.lastChatroomId = String(lastChatroomId);
  }

  const response = await axiosClient.get('/chatroom', {params});
  return response.data;
}
