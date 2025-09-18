import { MenuIcon } from "lucide-react";
import { useAuthStore } from "@/entities/user";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger
} from "@/shared/ui/dropdown-menu";
import {ThemeSwitcher} from "@/features/theme";
import {LogoutButton} from "@/features/user";

const MobileHeader = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <header className="p-4 border-b flex justify-between items-center z-40 bg-background lg:hidden">
      <div className="w-8 h-8 bg-gradient-to-r from-pink-500 to-red-500 rounded-lg flex items-center justify-center">
        {/* 로고 */}
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <MenuIcon className="w-6 h-6 cursor-pointer" />
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-56 rounded-xl">
          <ThemeSwitcher />
          {isAuthenticated && <LogoutButton />}
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  );
};

export default MobileHeader;