import { HomeIcon } from "lucide-react";
import { Button } from "@shared/ui/button.tsx";
import { Link } from "react-router-dom";
import { CreateTrigger } from "@/features/post/ui/create-trigger.tsx";
import { ChatTrigger } from "@/features/chat/ui/chat-trigger.tsx";
import { NotificationTrigger } from "@/features/notification/ui/notification-trigger.tsx";
import { AuthTrigger } from "@/features/user/ui/auth-trigger.tsx";

const MobileNavbar = () => {
  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 bg-background border-border lg:hidden">
      <div className="flex items-center justify-around py-2 px-4 max-w-md mx-auto">
        <Button variant="ghost" size="icon" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <Link to={'/'}>
            <HomeIcon className="cursor-pointer" />
          </Link>
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <NotificationTrigger />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <CreateTrigger />
        </Button>

        <Button variant="ghost" size="sm" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <ChatTrigger />
        </Button>

        <Button variant="ghost" size="icon" className="flex flex-col items-center gap-1 h-auto py-4 px-3">
          <AuthTrigger />
        </Button>
      </div>
    </div>
  )
}

export default MobileNavbar;