import {CommentEditor} from "@/features/comment/ui/CommentEditor.tsx";
import type {CommentCreateReq, CommentEntity} from "@/entities/comment/model/types.ts";
import {Controller, useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useCommentCreate} from "@/features/comment/api/useCommentCreate.ts";
import {commentCreateSchema} from "@/entities/comment/model/types.ts";
import {useCommentUpdate} from "@/features/comment/api/useCommentUpdate.ts";

interface CommentFormProps {
  postId: number
  commentToEdit?: CommentEntity;
  onCancel?: () => void;
}

export const CommentForm = ({postId, commentToEdit, onCancel}: CommentFormProps) => {
  const isEditMode = !!commentToEdit;

  const {
    handleSubmit,
    control,
    formState: {isSubmitting},
  } = useForm<CommentCreateReq>({
    resolver: zodResolver(commentCreateSchema),
    defaultValues: isEditMode ?
      {
        content: commentToEdit.content,
      }
      :
      {
        content: ''
      }
  });

  const {mutate: commentCreate} = useCommentCreate();
  const {mutate: commentUpdate} = useCommentUpdate();

  const onSubmit = (data: CommentCreateReq) => {
    if (isEditMode && commentToEdit) {
      commentUpdate({
        postId: postId,
        commentId: commentToEdit.commentId,
        data: data
      });
    } else {
      commentCreate({
        postId: postId,
        data: data as CommentCreateReq
      });
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Controller
        name={'content'}
        control={control}
        render={({field: {onChange, value}}) => (
          <CommentEditor
            data={value}
            onChange={onChange}
          />
        )}
      />

      {!isEditMode &&
        <button type={'submit'} disabled={isSubmitting}>
          {isSubmitting ? '댓글 저장 중...' : '댓글 작성하기'}
        </button>
      }

      {isEditMode &&
        <>
          <button type={'submit'} disabled={isSubmitting}>
            {isSubmitting ? '댓글 수정 중...' : '댓글 수정하기'}
          </button>
          <button type="button" onClick={onCancel}>수정취소</button>
        </>
      }
    </form>
  );
};