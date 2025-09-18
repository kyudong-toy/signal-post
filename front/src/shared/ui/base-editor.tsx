import StarterKit from '@tiptap/starter-kit'
import { useEditor, EditorContent, ReactNodeViewRenderer } from '@tiptap/react'
import { CharacterCount } from '@tiptap/extension-character-count'
import Image from '@tiptap/extension-image';
import { useMediaQuery } from "@shared/lib/useMediaQuery.ts";
import { EditorBubbleMenu } from "@shared/lib/tiptap/ui/bubble-menu.tsx";
import { SlashCommand } from "@shared/lib/tiptap/lib/slash-command.tsx";
import { Input } from "@shared/ui/input.tsx";
import React, {useEffect, useMemo, useRef, useState} from "react";
import { toast } from "sonner";
import { useFileUpload } from "@/features/file/api/fileUpload.ts";
import type { FileUploadRes } from "@/entities/file/model/types.ts";
import { ImageView } from "@shared/lib/tiptap/ui/image-view.tsx";
import {useThumbnailStore} from "@/features/post/model/thumbnailStore.ts";

interface EditorProps {
  data: object | string;
  onChange: (value: object) => void;
  limit: number;
  onUploadChange?: (isUploading: boolean) => void;
  onFileUploadSuccess?: (file: { id: number, webPath: string }) => void;
}

const baseHttpsURL = import.meta.env.VITE_HTTPS_BASE_URL || 'https://localhost';

export const Editor = ({ data, onChange, limit, onUploadChange, onFileUploadSuccess }: EditorProps) => {
  const isDesktop = useMediaQuery('(min-width: 1024px)');
  const [uploadingCount, setUploadingCount] = useState(0);
  const { setSelectedThumbnailUrl } = useThumbnailStore();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { mutate: fileUploadMutation, isPending: isUploading } = useFileUpload();

  const tiptapExtensions = useMemo(() => {
    return [
      StarterKit,
      CharacterCount.configure({
        limit: limit,
      }),
      Image.extend({
        addAttributes() {
          return {
            ...this.parent?.(),
            "data-temp-id": { default: null },
            class: { default: null },
            webPath: { default: null }, // 썸네일 선택을 위한 고유 경로
            fileId: { default: null },  // 파일 ID
          };
        },
        addNodeView() {
          return ReactNodeViewRenderer(ImageView);
        }
      }),
      SlashCommand
    ];
  }, [limit]);

  const editor = useEditor({
    extensions: tiptapExtensions,
    content: data || '',
    onUpdate: ({ editor }) => {
      onChange(editor.getJSON());
    },
    onTransaction: ({ editor }) => {
      const imageCount = editor.state.doc.content.toJSON().filter(
        (node: any) => node.type === 'image'
      ).length;

      if (imageCount > 3) {
        editor.chain().undo().run();
      }
    },
  });

  useEffect(() => {
    if (onUploadChange) {
      const isCurrentlyUploading = uploadingCount > 0;
      onUploadChange(isCurrentlyUploading);
    }
  }, [uploadingCount]);

  const handleFileSelectAndUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files || []);
    if (!files.length || !editor) return;

    if (onUploadChange) onUploadChange(true);
    setUploadingCount(prev => prev + files.length);
    const isFirstUploadInBatch = useThumbnailStore.getState().selectedThumbnailUrl === null;

    files.forEach((file, index) => {
      const localUrl = URL.createObjectURL(file);
      const tempId = `temp-${Date.now()}-${Math.random().toString(36).substring(2)}`;

      editor.chain().focus().insertContent({
        type: 'image',
        attrs: { src: localUrl, 'data-temp-id': tempId, class: 'is-uploading' }
      }).run();

      fileUploadMutation(file, {
        onSuccess: (data: FileUploadRes) => {
          if (data.webPath) {
            const fullUrl = import.meta.env.VITE_HTTPS_BASE_URL + data.webPath;

            if (index === 0 && isFirstUploadInBatch) {
              setSelectedThumbnailUrl(data.webPath);
            }

            editor.state.doc.descendants((node) => {
              if (node.type.name === 'image' && node.attrs['data-temp-id'] === tempId) {
                editor.chain().focus().updateAttributes('image', {
                  src: fullUrl,
                  class: 'rounded-lg border border-stone-200',
                  webPath: baseHttpsURL + data.webPath,
                  fileId: data.id,
                }).run();

                if (onFileUploadSuccess) {
                  onFileUploadSuccess(data);
                }
                return false;
              }
            });
            toast.success('업로드 성공!');
          }
        },
        onError: (error) => {
          console.error("파일 업로드 실패:", error);
          toast.error(`'${file.name}' 업로드 실패.`);
          editor.state.doc.descendants((node, pos) => {
            if (node.type.name === 'image' && node.attrs['data-temp-id'] === tempId) {
              editor.chain().focus().deleteRange({ from: pos, to: pos + node.nodeSize }).run();
              return false;
            }
          });
        },
        onSettled: () => {
          setUploadingCount(prev => prev - 1);
        }
      });
    });

    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  return (
    <div>
      <Input
        id="tiptap-image-upload"
        type="file"
        ref={fileInputRef}
        onChange={handleFileSelectAndUpload}
        className="hidden"
        accept="image/*"
        disabled={isUploading}
        multiple={true}
      />
      <div
        className="w-full min-h-[140px] max-h-[60vh] overflow-y-auto p-3 text-foreground bg-background border rounded-xl cursor-pointer"
        onClick={() => editor?.commands.focus()}
      >
        <EditorContent
          editor={editor}
          className="prose dark:prose-invert max-w-none focus:outline-none"
        />
        {isDesktop && <EditorBubbleMenu editor={editor} />}
      </div>
    </div>
  );
};

export default Editor;