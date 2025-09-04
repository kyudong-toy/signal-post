import {Controller, useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import type {ChatMessageCreateReq} from "@/entities/chat/model/model.ts";
import {useChatMessageCreateSchema} from "@/entities/chat/model/model.ts";
import {useChatMessageCreate} from "@/features/chat/api/useChatMessageCreate.ts";
import {CommentEditor} from "@/features/comment/ui/CommentEditor.tsx";

interface ChatMessageFormProps {
  roomId: number
}

export const ChatMessageForm = ({roomId}: ChatMessageFormProps) => {
  const {
    handleSubmit,
    control,
    formState: {isSubmitting},
  } = useForm<ChatMessageCreateReq>({
    resolver: zodResolver(useChatMessageCreateSchema),
    defaultValues: {
      content: '',
      messageType: 'TEXT',
    }
  });

  const {mutate: chatMessageCreate} = useChatMessageCreate();

  const onSubmit = (data: ChatMessageCreateReq) => {
    chatMessageCreate({
      roomId: roomId,
      data: data
    })
  }

  const onInvalid = (validationErrors: any) => {
    console.debug('❌ 폼 유효성 검사 실패:', validationErrors);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit, onInvalid)}>
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

      <button type={'submit'} disabled={isSubmitting}>
        {isSubmitting ? '전송 중...' : '메시지 전송'}
      </button>
    </form>
  );
};