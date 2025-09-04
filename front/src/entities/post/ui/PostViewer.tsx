import Output from "editorjs-react-renderer";
import type {PostContent} from "@/entities/post/model/types.ts";

interface PostViewerProps {
  content: string | PostContent
}

export const PostViewer = ({content}: PostViewerProps) => {
  const data = typeof content === 'string' ? JSON.parse(content) : content;
  return <Output data={data}/>
}