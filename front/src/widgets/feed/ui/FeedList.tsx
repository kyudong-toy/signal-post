import { FeedItemCard, useFeedsQuery } from "@/entities/feed";
import { useInView } from "react-intersection-observer";
import { Fragment, useEffect } from "react";

export const FeedList = () => {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useFeedsQuery();

  const { ref, inView } = useInView({
    threshold: 0,
  });

  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      void fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage])

  if (isLoading) {
    return <div>피드를 불러오는 중...</div>;
  }

  if (isError) {
    return <div>에러가 발생했습니다.</div>;
  }

  return (
    <div className="w-full max-w-4xl mx-auto space-y-4">
      {data?.pages.map((page, i) => (
        <Fragment key={i}>
          {page.content.map((feedItem) => (
            <FeedItemCard
              key={feedItem.content.postId}
              author={feedItem.author}
              content={feedItem.content}
            />
          ))}
        </Fragment>
      ))}

      {hasNextPage && (
        <div ref={ref} className="h-1" />
      )}

      {isFetchingNextPage && (
        <div className="text-center py-4">계속 읽어오는 중...</div>
      )}

      {/* 더 이상 다음 페이지가 없을 때 메시지를 표시 */}
      {!hasNextPage && (
        <div className="text-center py-4 text-gray-500">
          마지막 피드입니다...
        </div>
      )}
    </div>
  );
}