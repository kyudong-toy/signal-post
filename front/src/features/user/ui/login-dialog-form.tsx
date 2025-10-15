import { DialogDescription, DialogHeader, DialogTitle } from "@shared/ui/dialog.tsx";
import { Input } from "@shared/ui/input.tsx";
import { Button } from "@shared/ui/button.tsx";
import { useForm } from "react-hook-form";
import { useLoginSchema } from "@/entities/user";
import type { UserLoginReq } from "@/entities/user";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@shared/ui/form.tsx";
import { useLogin } from "../api/useLogin.ts";
import { toast } from "sonner";

interface LoginDialogProps {
  onSwitchToSignup: () => void;
  onLoginSuccess: () => void;
}

export const LoginDialogForm = ({ onSwitchToSignup, onLoginSuccess }: LoginDialogProps) => {
  const form = useForm<UserLoginReq>({
    resolver: zodResolver(useLoginSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const { mutate: login } = useLogin();

  const onSubmit = (data: UserLoginReq) => {
    login(data, {
      onSuccess: () => {
        form.reset();
        onLoginSuccess();
        toast.success('로그인에 성공하였습니다');
      },
      onError: () => {
        toast.warning('로그인에 실패하였습니다');
      }
    });
  };

  return (
   <>
    <DialogHeader>
      <DialogTitle>로그인</DialogTitle>
      <DialogDescription>
        환영합니다
      </DialogDescription>
    </DialogHeader>

    <Form { ...form }>
      <form
        onSubmit={ form.handleSubmit(onSubmit) }
        className="space-y-4"
      >
        <FormField
          control={ form.control }
          name="username"
          render={({ field }) => (
            <FormItem>
              <FormLabel htmlFor="username">아이디</FormLabel>
              <FormControl>
                <Input
                  id="username"
                  {...field}
                  type="text"
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
                  type="password"
                  placeholder="비밀번호를 입력하세요"
                  {...field}
                  autoComplete="current-password"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          { form.formState.isSubmitting ? '로그인 중...' : '로그인' }
        </Button>
      </form>
    </Form>
    <Button
      type="button"
      variant="link"
      size="sm"
      className="w-full"
      onClick={ onSwitchToSignup }
    >
      계정이 없으신가요? 회원가입
    </Button>
   </>
  );
};