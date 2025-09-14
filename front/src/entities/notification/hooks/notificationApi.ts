import axiosClient from "@shared/axios";
import type {NotificationEntity} from "@/entities/notification/model/type";

interface NotificationFindVariables {
  cursorId?: number | null
}

export const findNotification = async ({cursorId}: NotificationFindVariables): Promise<NotificationEntity> => {
  const params: Record<string, number> = {};

  if (cursorId) {
    params.cursorId = cursorId;
  }

  const response = await axiosClient.get(`/notification`, {params});
  return response.data;
}
