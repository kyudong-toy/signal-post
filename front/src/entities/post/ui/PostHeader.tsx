import {useNavigate} from "react-router-dom";
import {Button} from "@shared/ui/button.tsx";
import {ArrowLeft, MoreHorizontal} from "lucide-react";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";
import {dataUtils} from "@shared/utils/dataUtils.ts";
import type {PostEntity} from "@/entities/post";

interface PostProps {
  post: PostEntity;
}

export const PostHeader = ({ post }: PostProps) => {
  const navigate = useNavigate();

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <div className="flex bg-white dark:bg-black justify-between p-4 border-white dark:border-gray-800 ">
      <div className="flex items-center space-x-3">
        <Button onClick={handleGoBack} variant={"ghost"} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full">
          <ArrowLeft size={20} className="text-gray-600" />
        </Button>
        <Avatar className="w-10 h-10">
          <AvatarImage src="https://github.com/shadcn.png" />
          <AvatarFallback>기본이미지</AvatarFallback>
        </Avatar>
        <div className="flex items-center space-x-2">
          <span className="font-medium text-gray-800 dark:text-gray-200 text-sm">
            사용자이름
          </span>
          <span className="text-gray-400">•</span>
          <span className="text-gray-500 dark:text-gray-400 text-sm">
            {dataUtils(post.createdAt)}
          </span>
        </div>
      </div>
      <Button variant={"ghost"} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full">
        <MoreHorizontal size={20} className="text-gray-600" />
      </Button>
    </div>
  );
};

