import { useInfiniteQuery } from "@tanstack/react-query";
import { findNotification } from "@/entities/notification/hooks/notificationApi.ts";
import type { NotificationEntity } from "@/entities/notification/model/type.ts";

export const feedsQueryKeys = {
  all: ['notifications'] as const,
}

export const useNotificationQuery = () => {
  return useInfiniteQuery({
    queryKey: feedsQueryKeys.all,
    queryFn: ({ pageParam }) => findNotification({ cursorId: pageParam }),
    initialPageParam: null,
    getNextPageParam: (page: NotificationEntity) => {
      if (page.hasNext) {
        return page.cursorId;
      }
      return undefined;
    }
  });
}