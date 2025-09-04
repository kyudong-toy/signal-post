import {useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import type {ChatRoomCreateReq} from "@/entities/chat/model/model.ts";
import {useChatRoomCreateSchema} from "@/entities/chat/model/model.ts";
import {useRoomCreate} from "@/features/chat/api/useChatRoomCreate.ts";

export const ChatRoomForm = () => {
  const {
    register,
    handleSubmit,
    formState: {errors, isSubmitting},
  } = useForm<ChatRoomCreateReq>({
    resolver: zodResolver(useChatRoomCreateSchema),
    defaultValues: {
      roomname: '',
      userIds: [1, 2], // todo: 하드코딩 수정 필요
    }
  });
  const {mutate: roomCreate} = useRoomCreate();

  const onSubmit = (data: ChatRoomCreateReq) => {
    console.log('ㅇㅇㅇ11');
    roomCreate(data);
  }

  const onInvalid = (validationErrors: any) => {
    console.debug('❌ 폼 유효성 검사 실패:', validationErrors);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit, onInvalid)}>
      <h1>채팅방 생성</h1>
      <div>
        <label htmlFor={'roomname'}>채팅방 이름</label>
        <input
          id={'roomname'}
          {...register('roomname')}
          required={true}
        />
        {errors.roomname && <p style={{color: 'red'}}>{errors.roomname.message}</p>}
      </div>
      <button type={'submit'} disabled={isSubmitting}>
        {isSubmitting ? '생성중..' : '채팅방 생성'}
      </button>
    </form>
  );
};
