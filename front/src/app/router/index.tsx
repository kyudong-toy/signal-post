import {createBrowserRouter} from "react-router-dom";
import {BaseLayout} from "../BaseLayout.tsx";
import {userRoutes} from "@/features/user/routes/routes.tsx";
import {postRoutes} from "@/features/post/routes";
import {postRouter} from "@/entities/post/routes";
import FeedPage from "@/pages/feed";
import RoomCreatePage from "@/pages/chat-room-create";
import ChatRoomPage from "@/pages/chat-room-list";
import ChatMessage from "@/pages/chat-message";

export const router = createBrowserRouter([
  {
    path: '/',
    element: <BaseLayout />,
    children: [
      {
        path: '/',
        element: <FeedPage />
      },
      {
        path: '/chat',
        element: <RoomCreatePage />
      },
      {
        path: '/chatrooms',
        element: <ChatRoomPage />
      },
      {
        path: '/chatroom/:roomId',
        element: <ChatMessage />
      },
      ...userRoutes,
      ...postRoutes,
      ...postRouter
    ]
  }
]);