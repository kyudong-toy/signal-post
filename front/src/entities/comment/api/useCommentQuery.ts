import {useQuery} from "@tanstack/react-query";
import {findCommentsByPostId} from "@/entities/comment/api/commentApi.ts";

export const commentQueryKeys = {
  all: ['comments'] as const,
  detail: (postId: number) => [...commentQueryKeys.all, postId] as const
}

export const useCommentQuery = (postId: number) => {
  return useQuery({
    queryKey: commentQueryKeys.detail(postId),
    queryFn: () => findCommentsByPostId(postId),
    enabled: !!postId
  });
};