import {useInfiniteQuery} from "@tanstack/react-query";
import {findFeeds} from "@/entities/feed/api/feedApi.ts";

export const feedsQueryKeys = {
  all: ['feeds'] as const,
}

export const useFeedsQuery = () => {
  return useInfiniteQuery({
    queryKey: feedsQueryKeys.all,
    queryFn: ({pageParam}) => findFeeds(pageParam),
    initialPageParam: {
      size: 10
    },
    getNextPageParam: (lastPage) => {
      if (lastPage.hasNext) {
        return {
          lastFeedId: lastPage.lastFeedId,
          size: 10
        }
      }
      return undefined;
    }
  });
}