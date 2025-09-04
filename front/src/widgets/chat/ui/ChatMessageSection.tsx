import {ChatMessageList} from "@/widgets/chat/ui/ChatMessageList.tsx";
import {ChatMessageForm} from "@/features/chat/ui/ChatMessageForm.tsx";
import {useParams} from "react-router-dom";

export const ChatMessageSection = () => {
  const {roomId} = useParams<{roomId: string}>();

  if (!roomId) {
    return <div>잘못된 접근입니다</div>;
  }

  return (
    <section>
      <h2>채팅 메시지</h2>
      <ChatMessageList roomId={Number(roomId)} />
      <ChatMessageForm roomId={Number(roomId)} />
    </section>
  )
}