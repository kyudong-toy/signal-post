import {useCommentQuery} from "@/entities/comment/api/useCommentQuery.ts";
import {CommentList} from "@/widgets/comment/ui/CommentList.tsx";
import {CommentForm} from "@/features/comment/ui/CommentForm.tsx";

export const CommentSection = ({postId}: {postId: number}) => {
  const {data: commentList, isLoading, isError} = useCommentQuery(Number(postId));

  if (isLoading) {
    return <div>댓글을 불러오는 중...</div>;
  }

  if (isError) {
    return <div>에러가 발생했습니다.</div>;
  }

  if (!commentList) {
    return <div>작성된 댓글이 없습니다.</div>
  }

  return (
    <section>
      <h2>댓글</h2>
      <CommentList comments={commentList} />
      <CommentForm postId={postId} />
    </section>
  )
}