import { Link } from 'react-router-dom';
import { UserIcon } from 'lucide-react';
import { useAuthStore } from "@/entities/user/model/authStore";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@shared/ui/tooltip";
import { useMediaQuery } from "@shared/lib/useMediaQuery.ts";
import { useLoginDialogStore } from "@/features/user/model/loginDialogStore.ts";

export const AuthTrigger = () => {
  const isDesktop = useMediaQuery('(min-width: 1024px)');
  const { isAuthenticated } = useAuthStore();
  const { openDialog } = useLoginDialogStore();

  if (isAuthenticated) {
    return (
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger asChild>
            <Link to="/me">
              <UserIcon className="w-6 h-6" />
            </Link>
          </TooltipTrigger>
          <TooltipContent side={ 'right' }>
            프로필
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    );
  }

  if (isDesktop) {
    return (
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger asChild>
            <UserIcon className="w-6 h-6 cursor-pointer" onClick={ () => openDialog('login') } />
          </TooltipTrigger>
          <TooltipContent side={'right'}>로그인</TooltipContent>
        </Tooltip>
      </TooltipProvider>
    );
  }

  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <Link to="/login">
            <UserIcon className="w-6 h-6" />
          </Link>
        </TooltipTrigger>
        <TooltipContent side={ 'top' }>
          로그인
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
};