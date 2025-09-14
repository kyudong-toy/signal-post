import axiosClient from "../../../shared/axios";
import type {FeedEntity} from "../model/types.ts";

export const findFeeds = async (page:number): Promise<FeedEntity> => {
  const params: Record<string, string | number> = {
    page: page,
  };

  const response = await axiosClient.get(`/feeds`, {params});
  return response.data;
}
