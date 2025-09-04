import {CommentViewer} from "@/entities/comment/ui/CommentViewer.tsx";
import type {CommentEntity} from "@/entities/comment/model/types.ts";

interface CommentListProps {
  comments: CommentEntity[];
}

export const CommentList = ({comments}: CommentListProps) => {
  if (!comments || comments.length === 0) {
    return <div>작성된 댓글이 없습니다.</div>;
  }

  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <CommentViewer key={comment.commentId} comment={comment} />
      ))}
    </div>
  );
};