import DOMPurify from "dompurify";
import parse from "html-react-parser";
import { toast } from "sonner";

interface ViewerProps {
  content: string;
}

const ListViewer = ({ content }: ViewerProps) => {
  if (content === undefined) {
    return;
  }

  try {
    const parseContent = DOMPurify.sanitize(content);

    return (
      <div className={ `prose prose-sm max-w-none` }>
        {parse(parseContent)}
      </div>
    );
  } catch (error) {
    toast.warning('콘텐츠 로드에 실패했습니다');
    console.error(error);
  }
}

export default ListViewer