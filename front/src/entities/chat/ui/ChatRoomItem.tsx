import type {ChatRoomItem} from "@/entities/chat/model/model.ts";

interface ChatRoomItemProps {
  item: ChatRoomItem;
}

export const ChatRoom = ({item}: ChatRoomItemProps) => {
  return (
    <div style={{border: '1px solid #eee', padding: '16px', margin: '8px 0'}}>
      <strong>방 넘버 : {item.roomId}</strong>
      <p>{item.memberCount}</p>
    </div>
  );
};