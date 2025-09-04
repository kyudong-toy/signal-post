import {messagesQueryKeys, useChatMessagesQuery} from "@/entities/chat/api/useChatMessagesQuery.ts";
import {ChatMessage} from "@/entities/chat/ui/ChatMessageItem.tsx";
import {type InfiniteData, useQueryClient} from "@tanstack/react-query";
import {useAuthStore} from "@/entities/user/model/authStore.ts";
import {useEffect} from "react";
import type {ChatMessageEntity, ChatMessageItem} from "@/entities/chat/model/model.ts";

interface ChatMessageListProps {
  roomId: number
}

export const ChatMessageList = ({roomId}: ChatMessageListProps) => {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useChatMessagesQuery(Number(roomId));

  // 2. 앞으로 올 메시지를 받기 위한 WebSocket 구독 로직
  const queryClient = useQueryClient();
  const {isStompConnected, subscribe, unsubscribe} = useAuthStore();

  useEffect(() => {
    if (isStompConnected && roomId > 0) {
      const destination = `/topic/chatroom/${roomId}`;
      console.log(`✅ Subscribing to ${destination}`);

      type WebSocketMessage = {
        type: string;
        payload: ChatMessageItem;
      }

      const subscription = subscribe(destination, (wrapperMessage: WebSocketMessage) => {
        const newMessage = wrapperMessage.payload;

        // --- 여기가 실시간 업데이트의 최종 핵심 코드 ---
        queryClient.setQueryData<InfiniteData<ChatMessageEntity>>(
          messagesQueryKeys.detail(roomId),
          (oldData) => {
            // oldData가 없으면 아무것도 하지 않음 (중요)
            if (!oldData) {
              return oldData;
            }

            // '불변성'을 완벽하게 지키는 가장 안전한 업데이트 방식
            // 1. 가장 바깥 객체(InfiniteData)를 복사
            const newData = {
              ...oldData,
              // 2. pages 배열을 복사
              pages: oldData.pages.map((page, index) => {
                // 3. 첫 번째 페이지만 수정
                if (index === 0) {
                  return {
                    ...page, // 4. 페이지 객체 자체를 복사
                    content: [newMessage, ...page.content], // 5. 새 메시지를 추가한 새 content 배열 생성
                  };
                }
                // 나머지 페이지들은 그대로 반환
                return page;
              }),
            };

            return newData; // React가 변화를 감지할 수 있는 '완전히 새로운 객체'를 반환
          }
        );
      });

      return () => {
        if (subscription) {
          unsubscribe(subscription);
        }
      };
    }
  }, [isStompConnected, roomId, subscribe, unsubscribe, queryClient]);

  if (isLoading) {
    return <div>메시지를 불러오는 중...</div>;
  }

  if (isError) {
    return <div>에러가 발생했습니다.</div>;
  }

  const chatMessageItems = data?.pages.flatMap(page => page.content) || [];

  return (
    <div>
      {chatMessageItems.map(item => (
          <ChatMessage key={item.messageId} item={item} />
      ))}
      <button
        onClick={() => fetchNextPage()}
        disabled={!hasNextPage || isFetchingNextPage}
      >
        {isFetchingNextPage ? '로딩 중...' : hasNextPage ? '더 보기' : '메시지가 없어용~'}
      </button>
    </div>
  );
}