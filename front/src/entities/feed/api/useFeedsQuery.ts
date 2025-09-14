import { useInfiniteQuery } from "@tanstack/react-query";
import { findFeeds } from "./feedApi.ts";
import type {FeedEntity} from "../model/types.ts";

export const feedsQueryKeys = {
  all: ['feeds'] as const,
}

export const useFeedsQuery = () => {
  return useInfiniteQuery({
    queryKey: feedsQueryKeys.all,
    queryFn: ({pageParam}) => findFeeds(pageParam),
    initialPageParam: 0,
    getNextPageParam: (page: FeedEntity) => {
      if (page.hasNext) {
        return page.nextPage;
      }
      return undefined;
    }
  });
}