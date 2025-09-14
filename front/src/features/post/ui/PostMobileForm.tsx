import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {Button} from "@shared/ui/button.tsx";
import type {PostCreateReq, PostEntity, PostUpdateReq} from "../../../entities/post";
import {postCreateSchema, postUpdateSchema} from "../../../entities/post";
import { usePostCreate } from "../api/usePostCreate.ts";
import { usePostUpdate } from "../api/usePostUpdate.ts";
import { Form, FormControl, FormField, FormItem, FormMessage } from "@shared/ui/form.tsx";
import { Input } from "@shared/ui/input.tsx";
import { useAuth } from "../../../entities/user/hooks/useAuth.ts";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";
import { Editor } from "../../../shared/editor/BaseEditor.tsx";

interface PostFormProps {
  postToEdit?: PostEntity;
}

export const PostDialogForm = ({postToEdit}: PostFormProps) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // if (!isAuthenticated) {
  //   toast.warning('로그인 후 이용해주세요');
  //   navigate('/');
  //   return;
  // }

  const isEditMode = !!postToEdit;

  const form = useForm<PostCreateReq | PostUpdateReq>({
    resolver: zodResolver(isEditMode ? postUpdateSchema : postCreateSchema),
    defaultValues: isEditMode
      ?
      {
        subject: postToEdit.subject,
        contents: JSON.parse(postToEdit.content),
        fileIds: [],
        delFileIds: [],
      }
      :
      {
        subject: '',
        contents: {
          type: '',
          content: [],
        },
        fileIds: [],
      }
  });

  const {mutate: postCreate} = usePostCreate();
  const {mutate: postUpdate} = usePostUpdate();

  const onSubmit = (data: PostCreateReq | PostUpdateReq) => {
    console.log(data);
    // if (isEditMode && postToEdit) {
    //   postUpdate({
    //     postId: postToEdit.postId,
    //     data: data as PostUpdateReq
    //   });
    //   toast.success('게시글이 수정되었습니다');
    //   form.reset();
    // } else {
    //   postCreate(data as PostCreateReq, {
    //     onSuccess: () => {
    //       toast.success('게시글이 저장되었습니다');
    //       form.reset();
    //     },
    //     onError: () => {
    //       toast.warning('게시글이 저장에 실패했습니다');
    //     }
    //   });
    // }
  }

  return (
    <div className="w-full min-h-screen flex flex-col p-6">
      <div className="flex items-center justify-between mb-8">
        <div className="text-2xl font-semibold text-center flex-1">
          게시글 작성
        </div>
      </div>

      <Form {...form}>
        <form
          onSubmit={ form.handleSubmit(onSubmit) }
          className="w-full bg-white text-white flex-1 flex flex-col rounded-2xl p-5">
          <FormField
            control={form.control}
            name="subject"
            render={({ field }) => (
              <FormItem className="space-y-3">
                <FormControl>
                  <div className="relative">
                    <Input
                      placeholder="Title*"
                      {...field}
                      className="w-full h-14 px-4 border rounded-xl text-black placeholder:text-black text-lg focus:ring-0"
                      maxLength={300}
                    />
                  </div>
                </FormControl>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center">
                    <FormMessage />
                  </div>
                  <div className="text-zinc-400">{field.value?.length || 0}/300</div>
                </div>
              </FormItem>
            )}
          />

          <div className={ 'py-5' }>
            <FormField
              control={form.control}
              name={ 'contents' }
              render={({ field }) => (
                <FormItem>
                  <FormControl>
                    <Editor
                      data={ field.value }
                      onChange={ field.onChange }
                      size={ 'min-h-44' }
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          </div>

          <div className="flex justify-end space-x-4 py-3">
            <Button
              type="button"
              variant="outline"
              className="px-6 py-2 bg-transparent text-black"
            >
              Save Draft
            </Button>
            <Button
              type="submit"
              className="px-8 py-2 bg-blue-600 hover:bg-blue-700 text-white"
              disabled={form.formState.isSubmitting}
            >
              {form.formState.isSubmitting ? "저장 중..." : "Post"}
            </Button>
          </div>
        </form>
      </Form>
    </div>
  )
}