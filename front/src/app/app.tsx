import { router } from "./router";
import { ThemeProvider } from "./theme-provider.tsx";
import { RouterProvider } from "react-router-dom";
import { Toaster } from "@/shared/ui/sonner";
import { useInitializeApp } from "@/features/app-init/useInitializeApp.ts";
import {AuthPortal} from "@/features/user/ui/auth-portal.tsx";

const App = () => {
  const isInitialized = useInitializeApp();

  if (!isInitialized) {
    // todo: 로딩..
  }

  return (
    <>
      <ThemeProvider defaultTheme={ 'system' } storageKey={ 'signal-theme' }>
        <RouterProvider router={ router } />
      </ThemeProvider>

      <AuthPortal />
      <Toaster position={ 'top-center' } />
    </>
  )
}

export default App;