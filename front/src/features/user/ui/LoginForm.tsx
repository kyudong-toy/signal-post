import {useForm} from "react-hook-form";
import type {UserLoginReq} from "@/entities/user/model/types.ts";
import {useLoginSchema} from "@/entities/user/model/types.ts";
import {zodResolver} from "@hookform/resolvers/zod";
import {useLogin} from "../api/useLogin.ts";

export const LoginForm = () => {
  const {
    register,
    handleSubmit,
    formState: {errors, isSubmitting},
  } = useForm<UserLoginReq>({
    resolver: zodResolver(useLoginSchema)
  });

  const {mutate: login} = useLogin();

  const onSubmit = (data: UserLoginReq) => {
    login(data);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h1>로그인</h1>
      <div>
        <label htmlFor={'username'}>아이디</label>
        <input
          id={'username'}
          {...register('username')}
          required={true}
        />
        {errors.username && <p style={{ color: 'red' }}>{errors.username.message}</p>}
      </div>
      <div>
        <label htmlFor={'password'}>비밀번호</label>
        <input
          id={'password'}
          type={'password'}
          {...register('password')}
          required={true}
        />
        {errors.password && <p style={{ color: 'red' }}>{errors.password.message}</p>}
      </div>
      <button type={'submit'} disabled={isSubmitting}>
        {isSubmitting ? '로그인중..' : '로그인'}
      </button>
    </form>
  )
}