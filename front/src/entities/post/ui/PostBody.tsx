import {useState} from "react";
import {Viewer} from "@shared/editor/BaseViewer.tsx";
import {Heart} from "lucide-react";
import type {PostEntity} from "@/entities/post";

interface PostProps {
  post: PostEntity;
}

export const PostBody = ({ post }: PostProps) => {
  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);

  const handleLike = () => {
    setIsLiked(!isLiked);
    // API 호출 로직 추가
    setLikeCount(likeCount + 1);
  };

  return (
    <div className="p-6">
      {post.subject && (
        <div className="w-full h-auto flex mb-6">
          <h1 className="text-4xl font-bold dark:text-white">
            {post.subject}
          </h1>
        </div>
      )}
      <div className="w-full h-auto flex mb-6 min-h-[250px]">
        <div className="text-lg dark:text-gray-300">
          <Viewer content={post.content as string} />
        </div>
      </div>

      <div className="flex items-center space-x-2 pt-4 border-t border-gray-100 dark:border-gray-800">
        <button
          onClick={handleLike}
          className={`flex items-center space-x-1 p-2 rounded-full transition-colors ${
            isLiked
              ? 'text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20'
              : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800'
          }`}
        >
          <Heart size={20} className={isLiked ? 'fill-current' : ''} />
          <span className="text-sm">{likeCount}</span>
        </button>
      </div>
    </div>
  );
};