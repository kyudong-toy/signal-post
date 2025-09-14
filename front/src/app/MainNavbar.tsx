import {
  Bell,
  HomeIcon,
  MenuIcon,
  MessageCircle,
  MoonIcon,
  Music,
  SunIcon,
} from "lucide-react";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/shared/ui/tooltip";
import {Link, Outlet} from "react-router-dom";
import { AuthTrigger } from "../features/user/ui/AuthTrigger";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from "@/shared/ui/dropdown-menu";
import {useTheme} from "./themeProvider";
import {Button} from "@/shared/ui/button";
import {useAuth} from "../entities/user/hooks/useAuth";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription, AlertDialogFooter,
  AlertDialogHeader, AlertDialogTitle,
} from "@/shared/ui/alert-dialog";
import {toast} from "sonner";
import {useState} from "react";
import {CreateTrigger} from "../features/post/ui/CreateTrigger.tsx";
import {NotificationPanel} from "@/widgets/notification/ui/NotificationPanel.tsx";

const MainNavbar = () => {
  const [ activePanel, setActivePanel ] = useState(null);
  const { setTheme } = useTheme();
  const { clearAuth, isAuthenticated } = useAuth();
  const [ showLogoutDialog, setShowLogoutDialog ] = useState(false)

  const handleLogoutClick = () => {
    setShowLogoutDialog(true)
  }

  const handleLogout = () => {
    setShowLogoutDialog(false)
    clearAuth();
    toast.success('로그아웃이 완료되었습니다');
  }

  const openPanel = (panelName: string) => {
    setActivePanel(activePanel === panelName ? null : panelName);
  };

  return (
    <div className="flex h-screen">
      <div className="w-20 bg-white dark:bg-black flex flex-col items-center py-6 space-y-4 z-30">
        <div className="mb-4">
          <div className="w-8 h-8 bg-gradient-to-r from-pink-500 to-red-500 rounded-lg flex items-center justify-center">
            <Music size={16} className="text-white" />
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
          <Tooltip>
            <TooltipTrigger asChild>
              <MessageCircle className="cursor-pointer" onClick={() => openPanel('messages')} />
            </TooltipTrigger>
            <TooltipContent side={'right'}>메시지</TooltipContent>
          </Tooltip>
          <CreateTrigger />
          <Tooltip>
            <TooltipTrigger asChild>
              <Bell className="cursor-pointer" onClick={() => openPanel('notification')} />
            </TooltipTrigger>
            <TooltipContent side={'right'}>알림</TooltipContent>
          </Tooltip>
          <AuthTrigger />
        </TooltipProvider>

        <div className="flex-1" />

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <MenuIcon className="w-6 h-6 cursor-pointer" />
          </DropdownMenuTrigger>
          <DropdownMenuContent
            className="w-80 rounded-xl text-black"
            side="right"
            align="end"
            sideOffset={10}
          >
            <div className="p-4 border-zinc-700">
              <div className="flex items-center rounded-lg p-1 w-full">
                <Button
                  variant={ 'ghost' }
                  onClick={ () => setTheme("light") }
                  className={ `flex items-center justify-center flex-1 h-8 rounded-md text-sm transition-colors` }
                >
                  <SunIcon className="w-4 h-4" />
                </Button>
                <Button
                  variant={ 'ghost' }
                  onClick={ () => setTheme("dark") }
                  className={ 'flex items-center justify-center flex-1 h-8 rounded-md text-sm transition-colors' }
                >
                  <MoonIcon className="w-4 h-4" />
                </Button>
                <Button
                  variant={ 'ghost' }
                  onClick={ () => setTheme("system") }
                  className={ 'flex items-center justify-center flex-1 h-8 rounded-md text-sm transition-colors' }
                >
                  <span className={ 'text-lg' }>자동</span>
                </Button>
              </div>
            </div>
            {isAuthenticated &&
              <DropdownMenuItem
                className="p-4 text-base text-red-500 hover:bg-zinc-800 cursor-pointer"
                onClick={handleLogoutClick}
              >
                로그아웃
              </DropdownMenuItem>
            }
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Expandable Panel */}
      {activePanel && (
        <div className="dark:bg-black border-r bg-white z-20 animate-in slide-in-from-left duration-300">
          {activePanel === 'notification' && (
            <div className="w-[500px] space-y-4 text-black">
              <NotificationPanel />
            </div>
          )}

          {activePanel === 'messages' && (
            <div className="w-[600px] space-y-4 text-black">
              <ChatPanel />
            </div>
          )}
        </div>
      )}

      <div className="flex-1 relative px-5 overflow-y-auto">
        <Outlet />
      </div>

      <AlertDialog open={showLogoutDialog} onOpenChange={setShowLogoutDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>로그아웃 하실건가요?</AlertDialogTitle>
            <AlertDialogDescription>정말로??...</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleLogout}>로그아웃</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

export default MainNavbar;

import { Avatar, AvatarFallback, AvatarImage } from "@/shared/ui/avatar";
import { Input } from "@/shared/ui/input";
import { ScrollArea, ScrollBar } from "@/shared/ui/scroll-area";
import { Separator } from "@/shared/ui/separator";
import { Search, PenSquare } from "lucide-react";

// --- DUMMY DATA ---
// 실제로는 API로부터 받아올 데이터입니다.
const friends = [
  { id: 1, name: '사용자이름', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 2, name: 'TestUser2', avatarUrl: 'https://github.com/vercel.png' },
  { id: 3, name: '김민준', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 4, name: '이서아', avatarUrl: 'https://github.com/vercel.png' },
  { id: 5, name: '박지훈', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 6, name: '최유나', avatarUrl: 'https://github.com/vercel.png' },
  { id: 7, name: '정하윤', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 7, name: '정하윤', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 7, name: '정하윤', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 7, name: '정하윤', avatarUrl: 'https://github.com/shadcn.png' },
  { id: 7, name: '정하윤', avatarUrl: 'https://github.com/shadcn.png' },
];

const conversations = [
  {
    id: 1,
    name: '김민준',
    avatarUrl: 'https://github.com/shadcn.png',
    lastMessage: '네, 알겠습니다. 내일 뵐게요! 😄',
    timestamp: '오후 2:30',
    unreadCount: 2
  },
  {
    id: 2,
    name: '리액트 스터디 그룹',
    avatarUrl: 'https://github.com/vercel.png',
    lastMessage: '최유나: 다들 주말 잘 보내세요~',
    timestamp: '오전 11:15',
    unreadCount: 0
  },
  {
    id: 3,
    name: '박지훈',
    avatarUrl: 'https://github.com/shadcn.png',
    lastMessage: '사진을 보냈습니다.',
    timestamp: '어제',
    unreadCount: 5
  },
  {
    id: 4,
    name: '정하윤',
    avatarUrl: 'https://github.com/vercel.png',
    lastMessage: '고마워요! 덕분에 문제 해결했어요.',
    timestamp: '2025.09.10',
    unreadCount: 0
  },
  {
    id: 5,
    name: '정하윤',
    avatarUrl: 'https://github.com/vercel.png',
    lastMessage: '고마워요! 덕분에 문제 해결했어요.',
    timestamp: '2025.09.10',
    unreadCount: 0
  },
  {
    id: 6,
    name: '정하윤',
    avatarUrl: 'https://github.com/vercel.png',
    lastMessage: '고마워요! 덕분에 문제 해결했어요.',
    timestamp: '2025.09.10',
    unreadCount: 0
  },
  {
    id: 7,
    name: '정하윤',
    avatarUrl: 'https://github.com/vercel.png',
    lastMessage: '고마워요! 덕분에 문제 해결했어요.',
    timestamp: '2025.09.10',
    unreadCount: 0
  },
];

export const ChatPanel = () => {
  const [view, setView] = useState('list');
  const [selectedRoom, setSelectedRoom] = useState(null);

  const handleRoomClick = (conversation) => {
    setSelectedRoom(conversation);
    setView('room');
  };

  const handleBackToList = () => {
    setSelectedRoom(null);
    setView('list');
  };

  // 현재 뷰 상태에 따라 다른 컴포넌트를 렌더링
  if (view === 'room') {
    return <ChatRoom onBack={handleBackToList} />;
  }

  return (
    <div className="flex flex-col h-screen bg-white dark:bg-black text-black dark:text-white">
      {/* 1. 헤더 */}
      <div className="p-4 border-b">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">메시지</h2>
          <Button variant="ghost" size="icon">
            <PenSquare className="h-6 w-6" />
          </Button>
        </div>
        <div className="relative">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input placeholder="검색" className="pl-8" />
        </div>
      </div>

      {/* 2. 상단 친구 목록 (팔로우) */}
      <div className="p-4">
        <h3 className="text-sm font-semibold text-muted-foreground mb-2">새로운 대화</h3>
        <ScrollArea className="w-full whitespace-nowrap">
          <div className="flex space-x-4 pb-4">
            {friends.map((friend) => (
              <div key={friend.id} className="flex flex-col items-center space-y-1 w-16 cursor-pointer">
                <Avatar className="h-12 w-12 border-2 border-primary">
                  <AvatarImage src={friend.avatarUrl} alt={friend.name} />
                  <AvatarFallback>{friend.name.substring(0, 2)}</AvatarFallback>
                </Avatar>
                <span className="text-xs font-medium truncate w-full text-center">{friend.name}</span>
              </div>
            ))}
          </div>
          <ScrollBar orientation="horizontal" />
        </ScrollArea>
      </div>

      <Separator />

      {/* 3. 대화방 목록 */}
      <ScrollArea className="flex-grow">
        <div className="flex flex-col">
          {conversations.map((convo) => (
            <div key={convo.id} onClick={() => handleRoomClick(convo)} className="flex items-center gap-4 p-4 hover:bg-muted/50 cursor-pointer">
              <Avatar className="h-12 w-12">
                <AvatarImage src={convo.avatarUrl} alt={convo.name} />
                <AvatarFallback>{convo.name.substring(0, 2)}</AvatarFallback>
              </Avatar>
              <div className="grid gap-1 flex-grow">
                <p className="font-semibold">{convo.name}</p>
                <p className={`text-sm text-muted-foreground truncate ${convo.unreadCount > 0 && 'font-bold text-foreground'}`}>
                  {convo.lastMessage}
                </p>
              </div>
              <div className="flex flex-col items-end text-xs text-muted-foreground gap-1">
                <span>{convo.timestamp}</span>
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>
    </div>
  );
};

import { ArrowLeft, Phone, Video, Send, Smile, Paperclip } from "lucide-react";
import { cn } from "@/shared/lib/utils"; // shadcn/ui의 cn 유틸리티

// --- DUMMY DATA ---
// 실제로는 API로부터 받아올 데이터입니다.
const currentUser = { id: 1, name: '나' };
const otherUser = { id: 2, name: '김민준', avatarUrl: 'https://github.com/shadcn.png' };

const messages = [
  { id: 1, senderId: 2, content: '안녕하세요! 프로젝트 관련해서 여쭤볼 게 있어서 연락드렸습니다.', timestamp: '오후 2:30' },
  { id: 2, senderId: 1, content: '네, 안녕하세요! 편하게 말씀해주세요.', timestamp: '오후 2:31' },
  { id: 3, senderId: 2, content: '혹시 오늘 저녁에 잠깐 시간 괜찮으신가요? 줌으로 잠깐 이야기 나누면 좋을 것 같아서요.', timestamp: '오후 2:31' },
  { id: 4, senderId: 2, content: '30분 정도면 될 것 같아요!', timestamp: '오후 2:31' },
  { id: 5, senderId: 1, content: '네, 좋습니다. 7시쯤 어떠세요?', timestamp: '오후 2:35' },
  { id: 6, senderId: 2, content: '완전 좋아요! 그때 뵙겠습니다 😄', timestamp: '오후 2:36' },
  { id: 7, senderId: 1, content: '넵!', timestamp: '오후 2:37' },
  { id: 8, senderId: 1, content: '링크는 제가 보내드릴게요.', timestamp: '오후 2:37' },
];

// 메시지 말풍선 컴포넌트
const MessageBox = ({ message, isCurrentUser }) => {
  return (
    <div className={cn("flex items-end gap-2", isCurrentUser ? "justify-end" : "justify-start")}>
      {!isCurrentUser && (
        <Avatar className="h-8 w-8">
          <AvatarImage src={otherUser.avatarUrl} alt={otherUser.name} />
          <AvatarFallback>{otherUser.name.substring(0, 1)}</AvatarFallback>
        </Avatar>
      )}
      <div className={cn(
        "max-w-xs md:max-w-md p-3 rounded-2xl",
        isCurrentUser
          ? "bg-primary text-primary-foreground rounded-br-none"
          : "bg-muted rounded-bl-none"
      )}>
        <p className="text-sm">{message.content}</p>
      </div>
      {isCurrentUser && (
        <span className="text-xs text-muted-foreground">{message.timestamp}</span>
      )}
    </div>
  );
};

export const ChatRoom = ({ onBack }) => {
  return (
    <div className="flex flex-col h-screen bg-white dark:bg-black text-black dark:text-white">
      {/* 1. 헤더 */}
      <div className="p-3 border-b flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <Avatar className="h-10 w-10">
            <AvatarImage src={otherUser.avatarUrl} alt={otherUser.name} />
            <AvatarFallback>{otherUser.name.substring(0, 2)}</AvatarFallback>
          </Avatar>
          <div>
            <p className="font-bold">{otherUser.name}</p>
            <p className="text-xs text-green-500">온라인</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="icon">
            <Phone className="h-5 w-5" />
          </Button>
          <Button variant="ghost" size="icon">
            <Video className="h-5 w-5" />
          </Button>
        </div>
      </div>

      {/* 2. 메시지 스크롤 영역 */}
      <ScrollArea className="flex-grow p-4">
        <div className="flex flex-col gap-4">
          {/* 날짜 구분선 */}
          <div className="text-center text-xs text-muted-foreground my-2">
            --- 2025년 9월 12일 ---
          </div>
          {messages.map((msg) => (
            <MessageBox key={msg.id} message={msg} isCurrentUser={msg.senderId === currentUser.id} />
          ))}
        </div>
      </ScrollArea>

      {/* 3. 메시지 입력 창 */}
      <div className="p-3 border-t">
        <div className="relative">
          <Input
            placeholder="메시지를 입력하세요..."
            className="pr-24 pl-10"
          />
          <div className="absolute left-3 top-1/2 -translate-y-1/2 flex gap-2">
            <Smile className="h-5 w-5 text-muted-foreground cursor-pointer hover:text-foreground" />
            <Paperclip className="h-5 w-5 text-muted-foreground cursor-pointer hover:text-foreground" />
          </div>
          <Button size="icon" className="absolute right-2 top-1/2 -translate-y-1/2 h-8 w-8">
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
};