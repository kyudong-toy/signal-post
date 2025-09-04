import {useInfiniteQuery} from "@tanstack/react-query";
import {findChatRooms} from "@/entities/chat/api/chatRoomApi.ts";

export const feedsQueryKeys = {
  all: ['chatrooms'] as const,
}

export const useChatRoomsQuery = () => {
  return useInfiniteQuery({
    queryKey: feedsQueryKeys.all,
    queryFn: ({pageParam}) => findChatRooms(pageParam),
    initialPageParam: {},
    getNextPageParam: (lastPage) => {
      if (lastPage.hasNext) {
        return {
          lastChatroomId: lastPage.lastChatroomId,
          cursorTime: lastPage.lastActivityAt
        }
      }
      return undefined;
    }
  });
}