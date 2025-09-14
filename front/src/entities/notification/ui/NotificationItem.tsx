import { Heart, UserPlus } from "lucide-react";
import {Avatar, AvatarFallback, AvatarImage} from "@shared/ui/avatar.tsx";
import { Button } from "@shared/ui/button.tsx";
import type { NotificationItem } from "@/entities/notification/model/type.ts";

export const NotificationItemCard = (notifcation: NotificationItem) => {
  const sender = notifcation.sender;
  const content = notifcation.content;
  const context = content.cotext;
  const type = content.type;

  return (
    <div className={ `border-b p-4 flex gap-4 hover:bg-muted/50 cursor-pointer`}>
      <div className="text-muted-foreground mt-1">
        {type === 'FOLLOW' && <UserPlus />}
        {type === 'POST' && <Heart className="text-red-500 fill-current" />}
        {type === 'COMMENT' && <Heart className="text-red-500 fill-current" />}
      </div>

      <div className="flex-grow w-full">
        <div className="flex items-center mb-2">
          <Avatar className="w-7 h-7">
            <AvatarImage src="https://github.com/shadcn.png" />
            <AvatarFallback>기본이미지</AvatarFallback>
          </Avatar>
        </div>

        {/* 알림 내용 */}
        <p className="text-sm text-foreground mb-1">
          {type === 'COMMENT' &&
            <>
              <strong>{sender.senderName}</strong>님이 <strong>"{context.postSubject ?? context.postSummary}"</strong> 게시물에 댓글을 남겼습니다.
            </>
          }
          {type === 'FOLLOW' &&
            <>
              <strong>{sender.senderName}</strong>님이 회원님을 팔로우를 요청했습니다.
            </>
          }
          {type === 'POST' &&
            <>
              <strong>{sender.senderName}</strong>님이
              <strong>"{context.postSubject ?? context.postSummary}"</strong> 게시물을 좋아합니다.
            </>
          }
        </p>

        {/* 추가 정보 (댓글 내용, 썸네일 등) */}
        {type === 'COMMENT' && (
          <p className="text-sm p-2 bg-muted rounded-md my-2">"{context.commentContent}"</p>
        )}

        {/* 액션 버튼 */}
        {type === 'FOLLOW' && (
          <Button variant="secondary" size="sm" className="mt-2">팔로우 승낙</Button>
        )}

        <p className="text-xs text-muted-foreground mt-2">{content.createdAt}</p>
      </div>
    </div>
  )
}