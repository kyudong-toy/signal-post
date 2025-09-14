import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useLogin } from "../api/useLogin.ts";
import { Button } from "@shared/ui/button.tsx";
import type { UserLoginReq } from "../../../entities/user";
import { useLoginSchema } from "../../../entities/user";
import { Input } from "@shared/ui/input.tsx";
import { Label } from "@shared/ui/label.tsx";
import { ArrowLeft } from "lucide-react";

export const LoginForm = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<UserLoginReq>({
    resolver: zodResolver(useLoginSchema)
  });

  const { mutate: login } = useLogin();

  const onSubmit = (data: UserLoginReq) => {
    login(data);
  }

  const onInvalid = (validationErrors: any) => {
    console.debug('❌ 폼 유효성 검사 실패:', validationErrors);
  }

  return (
    <div className="fixed inset-0 z-50 bg-background md:hidden">
      <div className="flex flex-col h-full">
        <div className="flex items-center p-4 border-b">
          <Button variant="ghost" size="sm">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="text-lg font-semibold ml-2">로그인</h1>
        </div>
        <form className="flex-1 p-6 space-y-6" onSubmit={handleSubmit(onSubmit, onInvalid)}>
          <h1>로그인</h1>
          <div>
            <Label htmlFor={ 'username' }>아이디</Label>
            <Input
              id={ 'username' }
              type={ 'text' }
              placeholder={ '아이디를 입력하세요' }
              { ...register('username') }
              required={ true }
            />
            { errors.username && <p style={{ color: 'red' }}>{errors.username.message}</p> }
          </div>
          <div>
            <Label htmlFor={ 'password' }>비밀번호</Label>
            <Input
              id={ 'password' }
              type={ 'password' }
              placeholder={ '비밀번호를 입력하세요' }
              { ...register('password') }
              required={ true }
            />
            { errors.password && <p style={{ color: 'red' }}>{errors.password.message}</p> }
          </div>
          <Button type={ 'submit' } disabled={ isSubmitting } className="w-full h-12 text-base">
            { isSubmitting ? '로그인중..' : '로그인' }
          </Button>
        </form>
      </div>
    </div>
  )
}