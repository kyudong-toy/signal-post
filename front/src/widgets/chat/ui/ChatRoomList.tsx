import {useChatRoomsQuery} from "@/entities/chat/api/useChatRoomsQuery.ts";
import {ChatRoom} from "@/entities/chat/ui/ChatRoomItem.tsx";
import {Link} from "react-router-dom";

export const ChatRoomList = () => {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useChatRoomsQuery();

  if (isLoading) return <div>채팅방을 불러오는 중...</div>;
  if (isError) return <div>에러가 발생했습니다.</div>;

  const chatRoomItems = data?.pages.flatMap(page => page.content) || [];

  return (
    <div>
      {chatRoomItems.map(item => (
        <Link to={`/chatroom/${item.roomId}`}>
          <ChatRoom key={item.roomId} item={item} />
        </Link>
      ))}
      <button
        onClick={() => fetchNextPage()}
        disabled={!hasNextPage || isFetchingNextPage}
      >
        {isFetchingNextPage ? '로딩 중...' : hasNextPage ? '더 보기' : ''}
      </button>
    </div>
  );
}