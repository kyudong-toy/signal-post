import { HeartIcon, HomeIcon, PlusIcon, SearchIcon } from "lucide-react";
import { AuthTrigger } from "../features/user/ui/AuthTrigger.tsx";
import {Button} from "@shared/ui/button.tsx";

const MobileNavbar = () => {
  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 bg-background border-t border-border md:hidden">
      <div className="flex items-center justify-around py-2 px-4 max-w-md mx-auto">
        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <HomeIcon className="w-6 h-6" />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <SearchIcon className="w-6 h-6" />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <PlusIcon className="w-6 h-6" />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <HeartIcon className="w-6 h-6" />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <AuthTrigger />
        </Button>
      </div>
    </div>
  )
}

export default MobileNavbar;