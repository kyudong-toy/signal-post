import { useSignup } from "../api/useSignup.ts";
import { useForm } from "react-hook-form";
import { useSignupSchema } from "@/entities/user/model/types.ts";
import type { UserCreateReq } from "@/entities/user";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@shared/ui/button.tsx";
import { ArrowLeft } from "lucide-react";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@shared/ui/form.tsx";
import { Input } from "@shared/ui/input.tsx";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";

export const SignupForm = () => {
  const navigate = useNavigate();

  const handleBack = () => {
    navigate(-1);
  }

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
        form.reset();
        toast.success('회원가입에 성공했습니다');
        navigate('/login');
      },
      onError: () => {
        toast.warning('회원가입에 실패했습니다');
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
          <h1 className="text-lg font-semibold ml-2">회원가입</h1>
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

            <Button asChild variant="link" className="w-full">
              <Link to="/login" replace>
                계정이 이미 있으신가요? 로그인
              </Link>
            </Button>
          </form>
        </Form>
      </div>
    </div>
  )
}