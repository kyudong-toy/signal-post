import Header from '@editorjs/header';
import List from '@editorjs/list';
import ImageTool from '@editorjs/image';
import {fileUpload} from "@/features/file/api/fileUpload.ts";

// Editor.js에 어떤 도구들을 사용할지 정의하는 설정 객체입니다.
export const EDITOR_TOOLS = {
  header: Header,
  list: List,
  image: {
    class: ImageTool,
    config: {
      uploader: {
        async uploadByFile(file: File) {
          try {
            const response = await fileUpload(file);
            return {
              success: 1,
              file: {
                url: response.webPath
              },
            };
          } catch (error) {
            console.error('이미지 업로드 실패:', error);
            return 0;
          }
        },
      },
    },
  },
};