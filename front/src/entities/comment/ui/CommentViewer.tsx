import type {CommentEntity} from "@/entities/comment/model/types.ts";
import {useState} from "react";
import {CommentForm} from "@/features/comment/ui/CommentForm.tsx";
import DOMPurify from "dompurify";
import parse from 'html-react-parser';

interface CommentViewerProps {
  comment: CommentEntity
}

export const CommentViewer =({comment}: CommentViewerProps) => {
  const [isEditing, setIsEditing] = useState<boolean>(false);

  return (
    <div>
      {isEditing ? (
        <CommentForm
          postId={comment.postId}
          commentToEdit={comment}
          onCancel={() => setIsEditing(false)}
        />
      ) : (
        <>
          {parse(DOMPurify.sanitize(comment.content))}
          <button onClick={() => setIsEditing(true)}>
            수정
          </button>
        </>
      )}
    </div>
  );
}