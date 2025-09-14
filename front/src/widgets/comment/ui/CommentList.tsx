import {CommentViewer} from "@/entities/comment/ui/CommentViewer.tsx";
import {useCommentQuery} from "@/entities/comment/api/useCommentQuery.ts";
import { useInView } from "react-intersection-observer";
import {useEffect} from "react";

export const CommentList = ({ postId }: { postId: number }) => {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useCommentQuery(postId, 'NEW');

  const { ref, inView } = useInView({
    threshold: 0,
  });

  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      void fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage])

  if (isLoading) {
    return <div className="p-2 text-center text-gray-500">댓글을 불러오는 중...</div>;
  }

  if (isError) {
    return <div className="p-2 text-center text-gray-500">에러가 발생했습니다.</div>;
  }

  const allComments = data?.pages.flatMap(page => page.comments) ?? [];
  if (allComments.length === 0) {
    return <div>작성된 댓글이 없습니다. 첫 댓글을 작성해보세요!</div>;
  }

  return (
    <div className="">
      {allComments.map((comment) => (
        <CommentViewer
          key={ comment.content.id }
          postId={ postId }
          comment={ comment }
        />
      ))}

      {hasNextPage && (
        <div ref={ref} className="h-1" />
      )}

      {isFetchingNextPage && (
        <div className="text-center py-4">
          계속 읽어오는 중...
        </div>
      )}

      {(data?.pages && !hasNextPage) && (
        <div className="text-center py-4 text-gray-500">
          마지막 댓글입니다
        </div>
      )}
    </div>
  );
};