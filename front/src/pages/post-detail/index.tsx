import { useParams } from "react-router-dom";
import { usePostQuery } from "@/entities/post";
import { CommentList } from "@/widgets/comment/ui/CommentList.tsx";
import { CommentForm } from "@/features/comment/ui/CommentForm.tsx";
import { PostBody } from "@/entities/post/ui/PostBody.tsx";
import { PostHeader } from "@/entities/post/ui/PostHeader.tsx";

const PostDetailPage = () => {
  const {postId} = useParams<{postId: string}>();
  const {data: post, isLoading, isError} = usePostQuery(Number(postId));

  if (isLoading) {
    return <div>게시글을 불러오는 중...</div>;
  }

  if (isError || !post) {
    return <div>게시글을 찾을 수 없거나 에러가 발생했습니다.</div>;
  }

  return (
    <article className="flex-1 flex items-center justify-center p-8">
      <div className="bg-white dark:bg-black rounded-3xl shadow-2xl w-full max-w-2xl min-h-[600px] border-gray-200 flex flex-col">
        <div className='sticky top-0 z-10 rounded-e-2xl'>
          <PostHeader post={post} />
        </div>

        <div className="flex-1 overflow-y-auto rounded-b-3xl">
          <PostBody post={post} />
          <CommentList postId={Number(postId)} />
        </div>

        <div className=" sticky bottom-0 z-10 p-4 bg-white border-white dark:border-gray-800 shrink-0">
          <CommentForm postId={Number(postId)} />
        </div>
      </div>
    </article>
  );
};

export default PostDetailPage;