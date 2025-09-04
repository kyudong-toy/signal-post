import {useQuery} from "@tanstack/react-query";
import {findPostById} from "@/entities/post/api/postApi.ts";

export const postQueryKeys = {
  all: ['post'] as const,
  detail: (postId: number) => [...postQueryKeys.all, postId] as const
}

export const usePostQuery = (postId: number) => {
  return useQuery({
    queryKey: postQueryKeys.detail(postId),
    queryFn: () => findPostById(postId),
    enabled: !!postId
  });
};