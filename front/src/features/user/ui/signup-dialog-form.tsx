import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@shared/ui/dialog.tsx";
import { Input } from "@shared/ui/input.tsx";
import { Button } from "@shared/ui/button.tsx";
import { useForm } from "react-hook-form";
import type { UserCreateReq } from "@/entities/user";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@shared/ui/form.tsx";
import { useSignupSchema } from "@/entities/user";
import { useSignup } from "../api/useSignup.ts";
import { toast } from "sonner";
import { useEffect } from "react";

interface SignupDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSwitchToLogin: () => void;
  onSignupSuccess: () => void;
}

export const SignupDialogForm = ({ open, onOpenChange, onSwitchToLogin, onSignupSuccess }: SignupDialogProps) => {
  const form = useForm<UserCreateReq>({
    resolver: zodResolver(useSignupSchema),
    defaultValues: {
      username: "",
      password: "",
      displayName: "",
    },
  });

  const { mutate: signup } = useSignup();

  const onSubmit = (data: UserCreateReq) => {
    signup(data, {
      onSuccess: () => {
        onSignupSuccess();
        form.reset();
        toast.success('회원가입에 성공했습니다');
      },
      onError: () => {
        toast.warning('회원가입에 실패했습니다');
      }
    });
  };

  useEffect(() => {
    if (!open) {
      form.reset();
    }
  }, [open, form]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>회원가입</DialogTitle>
          <DialogDescription>
            회원가입 후 다양한 글을 접해보세요
          </DialogDescription>
        </DialogHeader>

        <Form { ...form }>
          <form onSubmit={ form.handleSubmit(onSubmit) } className="space-y-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel htmlFor="username">아이디</FormLabel>
                  <FormControl>
                    <Input
                      id="username"
                      {...field}
                      placeholder="아이디를 입력하세요"
                      autoComplete="username"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel htmlFor="password">비밀번호</FormLabel>
                  <FormControl>
                    <Input
                      id="password"
                      {...field}
                      type="password"
                      placeholder="비밀번호를 입력하세요"
                      autoComplete="new-password"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="displayName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel htmlFor="displayName">사용자 이름</FormLabel>
                  <FormControl>
                    <Input
                      id="displayName"
                      {...field}
                      type="text"
                      placeholder="화면에 표시할 이름을 입력하세요"
                      autoComplete="displayName"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
              { form.formState.isSubmitting ? '회원가입 중...' : '회원가입' }
            </Button>
          </form>
        </Form>
        <Button
          type="button"
          variant="link"
          size="sm"
          className="w-full"
          onClick={onSwitchToLogin}
        >
          계정이 이미 있으신가요? 로그인
        </Button>
      </DialogContent>
    </Dialog>
  );
};