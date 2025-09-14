import type {CommentItem} from "@/entities/comment/model/types.ts";
import {useState} from "react";
import {CommentForm} from "@/features/comment/ui/CommentForm.tsx";
import {Viewer} from "@shared/editor/BaseViewer.tsx";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";
import {dataUtils} from "@shared/utils/dataUtils.ts";
import {Button} from "@shared/ui/button.tsx";

interface CommentViewerProps {
  comment: CommentItem,
  postId: number
}

export const CommentViewer =({comment, postId}: CommentViewerProps) => {
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const content = comment.content;
  const author = comment.author;

  return (
    <div className="border-b border-gray-100 p-4">
      {/* 댓글 작성자 정보 */}
      <div className="flex items-center space-x-2 mb-2">
        <Avatar className="w-6 h-6">
          <AvatarImage src="https://github.com/shadcn.png" />
          <AvatarFallback className="text-xs">유저</AvatarFallback>
        </Avatar>
        <span className="text-sm font-medium text-gray-700">
          {author.username}
        </span>
        <span className="text-xs text-gray-500">
          {dataUtils(content.createdAt)}
        </span>
      </div>

      {/* 댓글 내용 */}
      <div className="ml-8">
        {isEditing ? (
          <CommentForm
            postId={postId}
            commentToEdit={comment}
            onCancel={() => setIsEditing(false)}
          />
        ) : (
          <>
            <Viewer content={content.content} />
            <Button
              onClick={() => setIsEditing(true)}
              className="text-xs text-gray-500 hover:text-gray-700"
            >
              수정
            </Button>
          </>
        )}
      </div>
    </div>
  );
}