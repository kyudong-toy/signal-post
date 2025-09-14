import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@shared/ui/button.tsx";
import type { PostCreateReq, PostEntity, PostUpdateReq } from "@/entities/post";
import { postRequestSchema } from "@/entities/post";
import { usePostCreate } from "../api/usePostCreate.ts";
import { usePostUpdate } from "../api/usePostUpdate.ts";
import { Form, FormControl, FormField, FormItem, FormMessage } from "@shared/ui/form.tsx";
import { Input } from "@shared/ui/input.tsx";
import { Editor } from "@shared/editor/BaseEditor.tsx";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@shared/ui/dialog.tsx";
import { toast } from "sonner";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";

interface PostDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  postToEdit?: PostEntity;
}

export const PostDialogForm = ({ open, onOpenChange, postToEdit }: PostDialogProps) => {
  const isEditMode = !!postToEdit;
  const title = isEditMode ? '게시글 수정' : '게시글 작성';

  const form = useForm<PostCreateReq | PostUpdateReq>({
    resolver: zodResolver(postRequestSchema),
    defaultValues: isEditMode ?
      {
        subject: postToEdit.subject,
        content: JSON.parse(postToEdit.content),
        fileIds: [],
        tags: [],
      }
      :
      {
        subject: '',
        content: {},
        fileIds: [],
        tags: [],
      }
  });

  const { mutate: postCreate } = usePostCreate();
  const { mutate: postUpdate } = usePostUpdate();

  const onSubmit = (data: PostCreateReq | PostUpdateReq) => {
    if (isEditMode && postToEdit) {
      postUpdate({
        postId: postToEdit.postId,
        data: data as PostUpdateReq
      }, {
        onSuccess: () => {
          toast.success('게시글이 수정되었습니다');
          form.reset();
          onOpenChange(false)
        },
        onError: () => {
          toast.warning('게시글이 수정에 실패했습니다');
        }
      });
    } else {
      postCreate(data as PostCreateReq, {
        onSuccess: () => {
          toast.success('게시글이 저장되었습니다');
          form.reset();
          onOpenChange(false)
        },
        onError: () => {
          toast.warning('게시글이 저장에 실패했습니다');
        }
      });
    }
  }

  return (
    <Dialog open={ open } onOpenChange={ onOpenChange }>
      <DialogContent className="max-w-2xl w-full">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div>
          <div className="flex items-center space-x-3">
            <Avatar className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold">
              <AvatarImage src="https://github.com/shadcn.png" />
              <AvatarFallback>기본이미지</AvatarFallback>
            </Avatar>
            <div className="text-sm font-medium">
              사용자이름
            </div>
          </div>
        </div>

        <Form { ...form }>
          <form onSubmit={ form.handleSubmit(onSubmit) }>
            <FormField
              control={ form.control }
              name="subject"
              render={({ field }) => (
                <FormItem>
                  <FormControl>
                    <Input
                      placeholder="Title"
                      { ...field }
                      className= {'mb-2 w-full h-10 border placeholder:text-black text-lg focus:ring-0'}
                      maxLength={300}
                    />
                  </FormControl>
                  <div className="flex items-center justify-between text-sm">
                    <div className="flex items-center">
                      <FormMessage />
                    </div>
                    <div className="text-zinc-400">{ field.value?.length || 0 }/300</div>
                  </div>
                </FormItem>
              )}
            />

            <div className={ 'py-5' }>
              <FormField
                control={form.control}
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
            </div>

            <div className="flex justify-end space-x-4 py-3">
              <Button
                type="submit"
                className="px-8 py-2 bg-blue-600 hover:bg-blue-700 text-white"
                disabled={ form.formState.isSubmitting }
              >
                { form.formState.isSubmitting ? "저장 중..." : "Post" }
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}