import type {FeedEntity} from "@/entities/feed";
import axiosClient from "@/shared/axios";

interface FeedFindVariables {
  lastFeedId?: number | null,
  size: number | 10
}

export const findFeeds = async ({lastFeedId, size}: FeedFindVariables): Promise<FeedEntity> => {
  const params: Record<string, string | number> = {
    size: size,
  };

  if (lastFeedId) {
    params.lastFeedId = lastFeedId;
  }

  const response = await axiosClient.get(`/feeds`, {params});
  return response.data;
}
