import type {FeedItem} from '../model/types';

interface FeedItemCardProps {
  item: FeedItem;
}

export const FeedItemCard = ({item}: FeedItemCardProps) => {
  return (
    <div style={{ border: '1px solid #eee', padding: '16px', margin: '8px 0' }}>
      <strong>{item.subject}</strong>
      <p>{item.content}</p>
      <small>{new Date(item.createdAt).toLocaleString()}</small>
    </div>
  );
};