import {FeedItemCard, useFeedsQuery} from "@/entities/feed";

export const FeedList = () => {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError
  } = useFeedsQuery();

  if (isLoading) return <div>피드를 불러오는 중...</div>;
  if (isError) return <div>에러가 발생했습니다.</div>;

  // flatMap을 사용해 2차원 배열(pages)을 1차원 배열로 만듭니다.
  const feedItems = data?.pages.flatMap(page => page.content) || [];

  return (
    <div>
      {feedItems.map(item => (
        <FeedItemCard key={item.postId} item={item} />
      ))}
      <button
        onClick={() => fetchNextPage()}
        disabled={!hasNextPage || isFetchingNextPage}
      >
        {isFetchingNextPage ? '로딩 중...' : hasNextPage ? '더 보기' : '마지막 피드입니다'}
      </button>
    </div>
  );
}