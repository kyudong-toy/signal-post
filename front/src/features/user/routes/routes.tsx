import LoginPage from "@/pages/login";
import SignupPage from "@/pages/signup";
import UpdatePage from "@/pages/update";

export const userRoutes = [
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/signup',
    element: <SignupPage />
  },
  {
    path: '/me',
    element: <UpdatePage />
  },
];