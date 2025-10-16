import { HomeIcon, MenuIcon } from "lucide-react";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/shared/ui/tooltip";
import { Link } from "react-router-dom";
import { DropdownMenu, DropdownMenuContent, DropdownMenuTrigger } from "@/shared/ui/dropdown-menu";
import { ThemeSwitcher } from "@/features/theme";
import { LogoutButton } from "@/features/user";
import { useAuthStore } from "@/entities/user";
import { NotificationTrigger } from "@/features/notification/ui/notification-trigger.tsx";
import { ChatTrigger } from "@/features/chat/ui/chat-trigger.tsx";
import { CreateTrigger } from "@/features/post/ui/create-trigger.tsx";
import { AuthTrigger } from "@/features/user/ui/auth-trigger.tsx";
import { LoginButton } from "@/features/user/ui/login-button.tsx";

interface DesktopNavbarProps {
  onPanelToggle: (panelName: string) => void;
}

const DesktopNavbar = ({ onPanelToggle }: DesktopNavbarProps) => {
  const { isAuthenticated } = useAuthStore();

  return (
    <header className="flex h-screen">
      <div className="w-16 bg-white dark:bg-black flex flex-col items-center py-6 space-y-4 z-30">
        <div className="mb-4">
          <div className="w-8 h-8 bg-gradient-to-r from-pink-500 to-red-500 rounded-lg flex items-center justify-center">
            {/* 로고 */}
          </div>
        </div>

        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
              <Link to={'/'}>
                <HomeIcon className="cursor-pointer" />
              </Link>
            </TooltipTrigger>
            <TooltipContent side={'right'}>홈</TooltipContent>
          </Tooltip>
          <NotificationTrigger onToggle={ () => onPanelToggle('notification') } />
          <CreateTrigger />
          <ChatTrigger onToggle={ () => onPanelToggle('chat') } />
          <AuthTrigger />
        </TooltipProvider>

        <div className="flex-1" />

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <MenuIcon className="w-6 h-6 cursor-pointer" />
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-80 rounded-xl text-black" side="right" align="end" sideOffset={10}>
            <ThemeSwitcher />
            { isAuthenticated ? <LogoutButton /> : <LoginButton /> }
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}

export default DesktopNavbar;
