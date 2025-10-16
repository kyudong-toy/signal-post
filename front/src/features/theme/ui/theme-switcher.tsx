import { MoonIcon, SunIcon } from "lucide-react";
import { useTheme } from "@/app/theme-provider.tsx";
import { Button } from "@/shared/ui/button";

export const ThemeSwitcher = () => {
  const { theme, setTheme } = useTheme();

  return (
    <div className="p-4 border-zinc-200 dark:border-zinc-700">
      <div className="flex items-center rounded-lg p-1 w-full bg-gray-200 dark:bg-zinc-800">
        <Button
          variant={'ghost'}
          onClick={() => setTheme("light")}
          className={`flex-1 h-8 rounded-md text-sm ${
            theme === 'light' ? 'bg-white dark:bg-zinc-700 shadow-sm' : ''
          }`}
        >
          <SunIcon className="w-4 h-4" />
        </Button>
        <Button
          variant={'ghost'}
          onClick={() => setTheme("dark")}
          className={`flex-1 h-8 rounded-md text-sm ${
            theme === 'dark' ? 'bg-white dark:bg-zinc-700 shadow-sm' : ''
          }`}
        >
          <MoonIcon className="w-4 h-4" />
        </Button>
        <Button
          variant={'ghost'}
          onClick={() => setTheme("system")}
          className={`flex-1 h-8 rounded-md text-sm ${
            theme === 'system' ? 'bg-white dark:bg-zinc-700 shadow-sm' : ''
          }`}
        >
          자동
        </Button>
      </div>
    </div>
  );
};