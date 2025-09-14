import { useState } from 'react';
import { Link } from 'react-router-dom';
import { UserIcon } from 'lucide-react';
import {useAuthStore} from "@/entities/user/model/authStore";
import {useMediaQuery} from "@shared/hooks/useMediaQuery";
import {LoginDialogForm} from "./LoginDialogForm";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@shared/ui/tooltip";
import {SignupDialogForm} from "./SignupDialogForm";
import {toast} from "sonner";

export const AuthTrigger = () => {
  const { accessToken } = useAuthStore();

  // 1. 로그인 되어 있을 경우: 프로필 페이지로 이동
  if (accessToken) {
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

  const isDesktop = useMediaQuery('(min-width: 768px)');
  const [ isLoginDialogOpen, setLoginDialogOpen ] = useState(false);
  const [ isSignupDialogOpen, setSignupDialogOpen ] = useState(false);

  // 로그인 -> 회원가입
  const handleSwitchToSignup = () => {
    setLoginDialogOpen(false);
    setSignupDialogOpen(true);
  };

  // 회원가입 -> 로그인
  const handleSwitchToLogin = () => {
    setSignupDialogOpen(false);
    setLoginDialogOpen(true);
  };

  // 회원가입 성공 후 -> 로그인
  const handleSignupSuccess = () => {
    toast.success('회원가입이 완료되었습니다!');
    setSignupDialogOpen(false);
    setLoginDialogOpen(true);
  };

  // 2. 비로그인 상태일 경우
  if (isDesktop) {
    // 2-1. PC: 아이콘 버튼 클릭 시 다이얼로그를 띄웁니다.
    return (
      <>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
            <UserIcon className="w-6 h-6 cursor-pointer" onClick={() => setLoginDialogOpen(true) } />
            </TooltipTrigger>
            <TooltipContent side={ 'right' }>
              로그인
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
        <LoginDialogForm
          open={ isLoginDialogOpen }
          onOpenChange={ setLoginDialogOpen }
          onSwitchToSignup={ handleSwitchToSignup }
        />
        <SignupDialogForm
          open={ isSignupDialogOpen }
          onOpenChange={ setSignupDialogOpen }
          onSwitchToLogin={ handleSwitchToLogin }
          onSignupSuccess={ handleSignupSuccess }
        />
      </>
    );
  } else {
    // 2-2. 모바일: 아이콘 클릭 시 모바일 전용 로그인 페이지로 이동합니다.
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
  }
};