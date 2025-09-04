import {Controller, useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import type {PostEntity, PostCreateReq, PostUpdateReq} from "@/entities/post/model/types.ts";
import {postCreateSchema, postUpdateSchema} from "@/entities/post/model/types.ts";
import PostEditor from "@/features/post/ui/PostEditor.tsx";
import {usePostUpdate} from "@/features/post/api/usePostUpdate.ts";
import {usePostCreate} from "@/features/post/api/usePostCreate.ts";

interface PostFormProps {
  postToEdit?: PostEntity;
}

export const PostForm = ({postToEdit}: PostFormProps) => {
  const isEditMode = !!postToEdit;

  const {
    register,
    handleSubmit,
    control,
    formState: {errors, isSubmitting},
  } = useForm<PostCreateReq | PostUpdateReq>({
    resolver: zodResolver(isEditMode ? postUpdateSchema : postCreateSchema),
    defaultValues: isEditMode
      ?
      {
        subject: postToEdit.subject,
        content: JSON.parse(postToEdit.content),
        fileIds: [],
        delFileIds: [],
      }
      :
      {
        subject: '',
        content: {
          time: new Date().getTime(),
          blocks: [],
          version: '2.30.8'
        },
        fileIds: [],
      }
  });

  const {mutate: postCreate} = usePostCreate();
  const {mutate: postUpdate} = usePostUpdate();

  const onSubmit = (data: PostCreateReq | PostUpdateReq) => {
    if (isEditMode && postToEdit) {
      postUpdate({
        postId: postToEdit.postId,
        data: data as PostUpdateReq
      });
    } else {
      postCreate(data as PostCreateReq);
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h1>게시글 작성하기</h1>
      <div>
        <label htmlFor={'subject'}>제목</label>
        <input id={'subject'} {...register('subject')} />
        {errors.subject && <p style={{color: 'red'}}>{errors.subject.message}</p>}
      </div>

      <div>
        <label>본문</label>
        <Controller
          name={'content'}
          control={control}
          render={({field: {onChange, value}}) => (
            <PostEditor
              data={value}
              onChange={onChange}
              holder="editor"
            />
          )}
        />
        {errors.content && <p style={{color: 'red'}}>{errors.content.message}</p>}
      </div>

      <button type={'submit'} disabled={isSubmitting}>
        {isSubmitting ? '저장 중...' : '저장하기'}
      </button>
    </form>
  )
}