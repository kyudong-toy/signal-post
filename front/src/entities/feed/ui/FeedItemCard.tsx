import type {FeedItem} from '../model/types';
import { Card, CardContent, CardHeader } from "@shared/ui/card";
import {Link} from "react-router-dom";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";
import {Button} from "@shared/ui/button.tsx";
import {dataUtils} from "@shared/utils/dataUtils.ts";
import {MoreHorizontal} from "lucide-react";
import {Viewer} from "@shared/editor/BaseViewer.tsx";
import {useAuthStore} from "@/entities/user/model/authStore.ts";
import {useFollow} from "@/features/follow/api/useFollow.ts";
import {toast} from "sonner";

export const FeedItemCard = (feed: FeedItem) => {
  const author = feed.author;
  const content = feed.content;
  const { isAuthenticated } = useAuthStore();

  const { mutate: follow } = useFollow();

  const handleFollow = () => {
    const username = author.username;
    follow({username}, {
      onSuccess: () => {
        toast.success('팔로우를 요청했습니다');
      },
      onError: () => {
        toast.success('잠시 후 다시 팔로우를 요청해주세요');
      }
    });
  }

  return (
    <Card key={content.postId} className="hover:shadow-lg transition-shadow cursor-pointer">
      <CardHeader>
        <div className="flex w-full items-center justify-between">
          <div className="flex items-center gap-3 text-sm text-muted-foreground">
            <Avatar>
              <AvatarImage src="https://github.com/shadcn.png" />
              <AvatarFallback>기본이미지</AvatarFallback>
            </Avatar>
            <div className="flex items-center space-x-2">
              <span>{author.username}</span>
              <span>•</span>
              <span>{dataUtils(content.createdAt)}</span>
            </div>
          </div>

          {/* 2. 오른쪽 요소 */}
          <div className="flex items-center space-x-2">
            {isAuthenticated &&
              <Button onClick={handleFollow} variant={"ghost"} className="bg-gray-100 hover:bg-gray-200 dark:hover:bg-gray-800 rounded-full">
                follow
              </Button>
            }
            <Button variant={"ghost"} className="bg-gray-100 hover:bg-gray-200 dark:hover:bg-gray-800 rounded-full">
              <MoreHorizontal size={20} className="text-gray-600" />
            </Button>
          </div>
        </div>
      </CardHeader>
      <Link to={ `/post/${content.postId}` }>
        <CardContent>
          <div className="flex items-start justify-between">
            <div className="space-y-2">
              {content.subject &&
                <div className="text-2xl hover:text-primary transition-colors">
                  { content.subject }
                </div>
              }
              <div className="text-base">
                <Viewer content={ content.content } />
              </div>
            </div>
          </div>
        </CardContent>
      </Link>
    </Card>
  );
};