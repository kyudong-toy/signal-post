import {PostForm} from "@/features/post/ui/PostForm.tsx";
import {useParams} from "react-router-dom";
import {usePostQuery} from "@/entities/post";

const PostUpdatePage = () => {
  const {postId} = useParams();
  const {data: post, isLoading} = usePostQuery(Number(postId));

  if (isLoading) {
    return <div>게시글 로딩중..</div>
  }

  return <PostForm postToEdit={post} />
}

export default PostUpdatePage;