import StarterKit from '@tiptap/starter-kit'
import DOMPurify from "dompurify";
import parse from "html-react-parser";
import { generateHTML } from "@tiptap/html";
import {toast} from "sonner";

interface ViewerProps {
  content: string;
}

export const Viewer = ({ content }: ViewerProps) => {
  if (content === undefined) {
    return;
  }

  try {
    const jsonContent = JSON.parse(content);
    const html = generateHTML(jsonContent, [StarterKit]);
    const parseContent = DOMPurify.sanitize(html);

    return (
      <div className={ `prose prose-sm max-w-none` }>
        {parse(parseContent)}
      </div>
    );
  } catch (error) {
    toast.warning('게시글 로드에 실패했습니다');
    console.error(error);
  }
}