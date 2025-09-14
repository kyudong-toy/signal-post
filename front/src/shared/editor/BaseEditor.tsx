import StarterKit from '@tiptap/starter-kit'
import { useEditor, EditorContent } from '@tiptap/react'
import { CharacterCount } from '@tiptap/extension-character-count'
import { BubbleMenu } from '@tiptap/react/menus'

interface EditorProps {
  data: {};
  onChange: (value: {}) => void;
}

export const Editor = ({data, onChange}: EditorProps) => {
  const editor = useEditor({
    extensions: [
      StarterKit,
      CharacterCount.configure({
        limit: 300, // 최대 300자
      }),
    ],
    content: data,
    onUpdate: ({editor}) => {
      onChange(editor.getJSON());
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
    <div
      className={ `w-full overflow-hidden min-h-32 p-3 text-black border rounded-xl cursor-pointer` }
      onClick={() => {
        editor?.commands.focus();
      }}
    >
      <EditorContent
        editor={editor}
        className="prose-none m-0 p-0"
      />

      <BubbleMenu className="bubble-menu" editor={editor}>
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          className={editor.isActive('heading', { level: 1 }) ? 'is-active' : ''}
        >
          H1
        </button>
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          className={editor.isActive('heading', { level: 2 }) ? 'is-active' : ''}
        >
          H2
        </button>
        <button
          onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
          className={editor.isActive('heading', { level: 3 }) ? 'is-active' : ''}
        >
          H3
        </button>
        <button
          onClick={() => editor.chain().focus().setParagraph().run()}
          className={editor.isActive('paragraph') ? 'is-active' : ''}
        >
          Paragraph
        </button>
        <button
          onClick={() => editor.chain().focus().toggleBold().run()}
          className={editor.isActive('bold') ? 'is-active' : ''}
        >
          Bold
        </button>
        <button
          onClick={() => editor.chain().focus().toggleItalic().run()}
          className={editor.isActive('italic') ? 'is-active' : ''}
        >
          Italic
        </button>
        <button
          onClick={() => editor.chain().focus().toggleStrike().run()}
          className={editor.isActive('strike') ? 'is-active' : ''}
        >
          Strike
        </button>
      </BubbleMenu>
    </div>
  )
}