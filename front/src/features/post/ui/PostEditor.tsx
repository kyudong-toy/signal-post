import {memo, useEffect, useRef} from 'react';
import EditorJS from '@editorjs/editorjs';
import {EDITOR_TOOLS} from '@/shared/config/editorTools';
import type {PostContent} from "@/entities/post/model/types.ts";

interface EditorProps {
  data?: PostContent;
  onChange: (data: PostContent) => void;
  holder: string;
}

const PostEditor = ({data, onChange, holder}: EditorProps) => {
  // Editor.js 인스턴스를 저장하기 위한 ref
  const ref = useRef<EditorJS | null>(null);

  // 컴포넌트가 마운트될 때 Editor.js를 초기화합니다.
  useEffect(() => {
    // 인스턴스가 없으면 새로 생성
    if (!ref.current) {
      ref.current = new EditorJS({
        holder: holder,
        tools: EDITOR_TOOLS,
        data: data,
        async onChange(api) {
          const savedData = await api.saver.save();
          onChange(savedData);
        },
      });
    }

    // 컴포넌트가 언마운트될 때 인스턴스를 파괴하여 메모리 누수를 방지합니다.
    return () => {
      if (ref.current && ref.current.destroy) {
        ref.current.destroy();
        ref.current = null;
      }
    };
  }, []);

  return <div id={holder} />;
};

export default memo(PostEditor);