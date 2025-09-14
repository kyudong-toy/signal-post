import type { CommentCreateReq, CommentEntity, CommentUpdateReq } from "@/entities/comment/model/types.ts";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCommentCreate } from "@/features/comment/api/useCommentCreate.ts";
import { useCommentUpdate } from "@/features/comment/api/useCommentUpdate.ts";
import { FormControl, FormField, FormItem, FormMessage } from "@shared/ui/form.tsx";
import { commentRquestSchema } from "@/entities/comment/model/types.ts";
import { toast } from "sonner";
import { Editor } from "@shared/editor/BaseEditor.tsx";
import { Button } from "@shared/ui/button.tsx";
import {useAuth} from "@/entities/user/hooks/useAuth.ts";

interface CommentFormProps {
  postId: number
  onCancel?: () => void;
  commentToEdit?: CommentEntity;
}

export const CommentForm = ({ postId, commentToEdit, onCancel }: CommentFormProps) => {
  const isEditMode = !!commentToEdit;
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return;
  }

  const form = useForm<CommentCreateReq | CommentUpdateReq>({
    resolver: zodResolver(commentRquestSchema),
    defaultValues: isEditMode ?
      {
        content: JSON.parse(commentToEdit.content)
      }
      :
      {
        content: {}
      }
  });

  const { mutate: commentCreate } = useCommentCreate();
  const { mutate: commentUpdate } = useCommentUpdate();

  const onSubmit = (data: CommentCreateReq) => {
    if (isEditMode && commentToEdit) {
      commentUpdate({
        postId: postId,
        commentId: commentToEdit.commentId,
        data: data as CommentUpdateReq
      }, {
        onSuccess: () => {
          toast.success('댓글이 수정되었습니다');
          form.reset();
        },
        onError: () => {
          toast.success('댓글 수정에 실패했습니다');
        }
      });
    } else {
      commentCreate({
        postId: postId,
        data: data as CommentCreateReq
      }, {
        onSuccess: () => {
          toast.success('댓글이 등록되었습니다');
          form.reset();
        },
        onError: () => {
          toast.success('댓글 등록에 실패했습니다');
        }
      });
    }
  }

  return (
    <FormProvider { ...form }>
      <form onSubmit={ form.handleSubmit(onSubmit) }>
        <FormField
          control={ form.control }
          name={ 'content' }
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Editor
                  data={ field.value }
                  onChange={ field.onChange }
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {!isEditMode &&
          <Button
            type={ 'submit' }
            disabled={ form.formState.isSubmitting }
          >
            { form.formState.isSubmitting ? '댓글 저장 중...' : '댓글 작성하기' }
          </Button>
        }

        {isEditMode &&
          <>
            <Button
              type={ 'submit' }
              disabled={ form.formState.isSubmitting }
            >
              { form.formState.isSubmitting ? '댓글 수정 중...' : '댓글 수정하기' }
            </Button>
            <Button type="button" onClick={onCancel}>
              수정취소
            </Button>
          </>
        }
      </form>
    </FormProvider>
  );
};