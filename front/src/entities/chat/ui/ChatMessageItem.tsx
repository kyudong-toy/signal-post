import type {ChatMessageItem} from "@/entities/chat/model/model.ts";

interface ChatMessageItemProps {
  item: ChatMessageItem;
}

export const ChatMessage = ({item}: ChatMessageItemProps) => {
  return (
    <div style={{border: '1px solid #eee', padding: '16px', margin: '8px 0'}}>
      <strong>발신자 : {item.senderId}</strong>
      <p>{item.content}</p>
    </div>
  );
};