import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@shared/ui/dialog.tsx";
import {Input} from "@shared/ui/input.tsx";
import {Button} from "@shared/ui/button.tsx";
import {useForm} from "react-hook-form";
import type {UserCreateReq} from "../../../entities/user";
import {zodResolver} from "@hookform/resolvers/zod";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@shared/ui/form.tsx";
import {useSignupSchema} from "../../../entities/user";
import {useSignup} from "../api/useSignup.ts";
import { toast } from "sonner";

interface SignupDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSwitchToLogin: () => void;
  onSignupSuccess: () => void;
}

export const SignupDialogForm = ({ open, onOpenChange, onSwitchToLogin, onSignupSuccess }: SignupDialogProps) => {
  // 1. useForm 훅을 사용해 form 객체를 생성합니다.
  const form = useForm<UserCreateReq>({
    resolver: zodResolver(useSignupSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const { mutate: signup } = useSignup();

  // 2. 폼 제출 시 실행될 함수를 정의합니다.
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

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>회원가입</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>아이디</FormLabel>
                  <FormControl>
                    <Input placeholder="아이디를 입력하세요" {...field} />
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
                  <FormLabel>비밀번호</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="비밀번호를 입력하세요" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
              {form.formState.isSubmitting ? '로그인 중...' : '로그인'}
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