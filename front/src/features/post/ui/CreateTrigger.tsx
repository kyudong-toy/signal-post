import { Link } from 'react-router-dom';
import { PlusIcon, UserIcon } from 'lucide-react';
import { useAuthStore } from "@/entities/user/model/authStore.ts";
import { useMediaQuery } from "@shared/hooks/useMediaQuery.ts";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@shared/ui/tooltip.tsx";
import { PostDialogForm } from "@/features/post";
import { useState } from "react";

export const CreateTrigger = () => {
  const { accessToken } = useAuthStore();

  // 1. 로그인 되어 있을 않을 경우: 로그인으로 이동
  if (!accessToken) {
    // todo: ㅜㅡㅜ
  }

  const isDesktop = useMediaQuery('(min-width: 768px)');
  const [ isPostDialogOpen, setPostDialogOpen ] = useState(false);

  // 2. 비로그인 상태일 경우
  if (isDesktop) {
    // 2-1. PC: 아이콘 버튼 클릭 시 다이얼로그를 띄웁니다.
    return (
      <>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild>
            <PlusIcon className="w-6 h-6 cursor-pointer" onClick={() => setPostDialogOpen(true) } />
            </TooltipTrigger>
            <TooltipContent side={ 'right' }>
              게시글 작성
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
        <PostDialogForm
          open={ isPostDialogOpen }
          onOpenChange={ setPostDialogOpen }
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