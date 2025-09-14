import {createBrowserRouter} from "react-router-dom";
import {BaseLayout} from "../BaseLayout";
import FeedPage from "../../pages/feed";
import PostDetailPage from "@/pages/post-detail";
import ProfilePage from "@/pages/profile";

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
        path: '/post/:postId',
        element: <PostDetailPage />
      },
      {
        path: '/me',
        element: <ProfilePage />
      }
    ]
  }
]);