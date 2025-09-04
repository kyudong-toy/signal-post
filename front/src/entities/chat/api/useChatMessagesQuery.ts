import {useInfiniteQuery} from "@tanstack/react-query";
import {findMessages} from "@/entities/chat/api/chatMessagesApi.ts";

export const messagesQueryKeys = {
  all: ['messages'] as const,
  detail: (roomId: number) => [...messagesQueryKeys.all, roomId] as const
}

export const useChatMessagesQuery = (roomId: number) => {
  return useInfiniteQuery({
    queryKey: messagesQueryKeys.detail(roomId),
    queryFn: ({pageParam}) => findMessages({
      roomId: roomId,
      cursorId: pageParam.cursorId,
      cursorTime: pageParam.cursorTime
    }),
    initialPageParam: {},
    getNextPageParam: (lastPage) => {
      if (lastPage.hasNext) {
        return {
          cursorId: lastPage.cursorId,
          cursorTime: lastPage.cursorTime
        }
      }
      return undefined;
    },
    enabled: !!roomId
  });
}