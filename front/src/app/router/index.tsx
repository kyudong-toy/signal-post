import { createBrowserRouter } from "react-router-dom";
import FeedPage from "@/pages/feed";
import PostDetailPage from "@/pages/post-detail";
import ProfilePage from "@/pages/profile";
import MobileRouteCover from "@/app/router/mobile-route.tsx";
import LoginPage from "@/pages/login";
import SignupPage from "@/pages/signup";
import PostCreatePage from "@/pages/post-create";
import BaseLayout from "@/widgets/base-layout/ui/base-layout.tsx";

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
  },
  {
    path: '/login',
    element: (
      <MobileRouteCover>
        <LoginPage />
      </MobileRouteCover>
    )
  },
  {
    path: '/signup',
    element: (
      <MobileRouteCover>
        <SignupPage />
      </MobileRouteCover>
    )
  },
  {
    path: '/write',
    element: (
      <MobileRouteCover>
        <PostCreatePage />
      </MobileRouteCover>
    )
  }
]);