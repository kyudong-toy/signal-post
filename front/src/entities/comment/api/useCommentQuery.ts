import type { CommentEntity } from "@/entities/comment";
import { useInfiniteQuery } from "@tanstack/react-query";
import { findComments } from "@/entities/comment/api/commentApi";

export const commentQueryKeys = {
  all: ['comments'] as const,
  lists: (postId: number, sort: string) => [...commentQueryKeys.all, postId, { sort }] as const,
}

export const useCommentQuery = (postId: number, sort: string) => {
  return useInfiniteQuery({
    queryKey: commentQueryKeys.lists(postId, sort),
    queryFn: ({ pageParam }): Promise<CommentEntity> => findComments({ postId, sort, cursorId: pageParam }),
    initialPageParam: undefined,
    getNextPageParam: (page: CommentEntity) => {
      if (page.hasNext) {
        return page.cursorId
      }
      return undefined;
    }
  });
};