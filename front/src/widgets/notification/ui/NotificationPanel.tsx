import { Card } from "@shared/ui/card.tsx";
import { Button } from "@shared/ui/button.tsx";
import { Settings } from "lucide-react";
import { Tabs, TabsList, TabsTrigger } from "@shared/ui/tabs.tsx";
import { ScrollArea } from "@shared/ui/scroll-area.tsx";
import { useNotificationQuery } from "@/entities/notification/hooks/useNotificationQuery.ts";
import { useInView } from "react-intersection-observer";
import { useEffect } from "react";
import { NotificationItemCard } from "@/entities/notification/ui/NotificationItem.tsx";

export function NotificationPanel() {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useNotificationQuery();

  const { ref, inView } = useInView({
    threshold: 0,
  });

  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      void fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage])

  if (isLoading) {
    return <div>알림을 불러오는 중...</div>;
  }

  if (isError) {
    return <div>에러가 발생했습니다.</div>;
  }

  const allNotification = data?.pages.flatMap(page => page.contents) ?? [];
  return (
    <Card className="h-screen rounded-none border-l border-t-0 border-b-0 border-r-0 flex flex-col">
      {/* 1. 헤더 */}
      <div className="p-4 border-b">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">알림</h2>
          <Button variant="ghost" size="icon">
            <Settings className="h-5 w-5" />
          </Button>
        </div>
      </div>

      {/* 2. 필터 탭 */}
      <Tabs defaultValue="all" className="p-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="all">전체</TabsTrigger>
          <TabsTrigger value="unread">안 읽음</TabsTrigger>
          <TabsTrigger value="mentions">@멘션</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* 3. 알림 목록 */}
      <ScrollArea className="flex-grow">
        <div className="flex flex-col">
          {allNotification.map((item) => (
            <NotificationItemCard
              key={ item.content.id }
              sender={ item.sender }
              content={ item.content }
            />
          ))}

          {allNotification.length === 0 &&
            <div className="p-2 text-center text-gray-500">알림이 없습니다.</div>
          }

          {hasNextPage && (
            <div ref={ref} className="h-1" />
          )}
        </div>
      </ScrollArea>
    </Card>
  );
}