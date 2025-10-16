import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle
} from "@shared/ui/alert-dialog.tsx";
import {useEffect, useState} from "react";
import { useLogoutAlertStore } from "@/features/user/model/logoutAlertStore.ts";
import { useAuthStore } from "@/entities/user";
import { toast } from "sonner";

export const LogoutAlert = () => {
  const { view, closeAlert } = useLogoutAlertStore();
  const { clearAuth } = useAuthStore();
  const [renderedView, setRenderedView] = useState(view);

  useEffect(() => {
    if (view !== null) {
      setRenderedView(view);
    }
  }, [view]);

  const handleLogoutConfirm = () => {
    toast.loading('로그아웃 중.....');
    setTimeout(() => {
      clearAuth();
      window.location.href = '/';
    }, 800);
  };

  return (
    <AlertDialog open={ !!view } onOpenChange={() => closeAlert()}>
      {renderedView === 'logout' && (
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>로그아웃 하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>이 작업은 되돌릴 수 없습니다.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleLogoutConfirm}>로그아웃</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      )}
    </AlertDialog>
  );
}