import { useEditor, EditorContent, type Editor } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import {CharacterCount} from '@tiptap/extension-character-count'

const Toolbar = ({editor}: {editor: Editor | null}) => {
  if (!editor) {
    console.error('에디터 생성에 에러가 발생했습니다');
    return null;
  }

  return (
    <div className="comment-toolbar">
      <button
        onClick={() => editor.chain().focus().toggleBold().run()}
        disabled={!editor.can().chain().focus().toggleBold().run()}
        className={editor.isActive('bold') ? 'is-active' : ''}
      >
        Bold
      </button>
      <button
        onClick={() => editor.chain().focus().toggleItalic().run()}
        disabled={!editor.can().chain().focus().toggleItalic().run()}
        className={editor.isActive('italic') ? 'is-active' : ''}
      >
        Italic
      </button>
      {/* 이미지/GIF 버튼도 이런 식으로 추가할 수 있습니다. */}
    </div>
  )
}

interface CommentEditorProps {
  data?: string;
  onChange: (value: string) => void;
}

export const CommentEditor = ({data, onChange}: CommentEditorProps) => {
  const editor = useEditor({
    extensions: [
      StarterKit,
      CharacterCount.configure({
        limit: 300, // 최대 300자
      }),
    ],
    content: data,
    onUpdate: ({editor}) => {
      onChange(editor.getHTML());
    },
    onTransaction: ({editor}) => {
      const imageCount = editor.state.doc.content.toJSON().filter(
        (node: any) => node.type === 'image'
      ).length;

      if (imageCount > 3) {
        editor.chain().undo().run();
      }
    },
  })

  return (
    <>
      <Toolbar editor={editor} />
      <EditorContent editor={editor} />
    </>
  )
}