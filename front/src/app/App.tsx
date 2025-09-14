import { router } from "./router";
import { ThemeProvider } from "./themeProvider";
import { RouterProvider } from "react-router-dom";
import { Toaster } from "@/shared/ui/sonner";

const App = () => {
  return (
    <ThemeProvider defaultTheme={ 'system' } storageKey={ 'signal-theme' }>
      <RouterProvider router={ router } />

      {/* 알림 토스트 */}
      <Toaster position={ 'top-center' } />
    </ThemeProvider>
  )
}

export default App;