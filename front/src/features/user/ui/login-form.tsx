import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useLogin } from "../api/useLogin.ts";
import { Button } from "@shared/ui/button.tsx";
import type { UserLoginReq } from "@/entities/user";
import { useLoginSchema } from "@/entities/user";
import { Input } from "@shared/ui/input.tsx";
import { ArrowLeft } from "lucide-react";
import {Link, useNavigate} from "react-router-dom";
import {Form, FormControl, FormField, FormItem, FormLabel, FormMessage} from "@shared/ui/form.tsx";
import {toast} from "sonner";

export const LoginForm = () => {
  const navigate = useNavigate();

  const handleBack = () => {
    navigate(-1);
  }

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
        toast.success('로그인에 성공하였습니다');
        handleBack();
      },
      onError: () => {
        toast.warning('로그인에 실패하였습니다');
      }
    });
  };

  return (
    <div className="fixed inset-0 z-50 bg-background">
      <div className="flex flex-col h-full">
        <div className="flex items-center p-4 border-b">
          <Button variant="ghost" size="sm" onClick={handleBack}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="text-lg font-semibold ml-2">로그인</h1>
        </div>
        <Form { ...form }>
          <form
            onSubmit={ form.handleSubmit(onSubmit) }
            className="flex-1 p-5 space-y-6"
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

            <Button asChild variant="link" className="w-full">
              <Link to="/signup" replace>
                계정이 없으신가요? 회원가입
              </Link>
            </Button>
          </form>
        </Form>
      </div>
    </div>
  )
}