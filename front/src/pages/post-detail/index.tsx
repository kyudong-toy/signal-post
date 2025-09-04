import {useParams} from "react-router-dom";
import {PostViewer, usePostQuery} from "@/entities/post";
import {dataUtils} from "@/shared/utils/dataUtils.ts";
import {CommentSection} from "@/widgets/comment/ui/CommentSection.tsx";

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
    <article>
      <h1>{post.subject}</h1>
      <p>
        <span>작성일: {dataUtils(post.createdAt)}</span>
      </p>
      <hr />
      <PostViewer content={post.content} />

      <hr style={{marginTop: '40px'}} />

      <CommentSection postId={Number(postId)} />
    </article>
  );
};

export default PostDetailPage;