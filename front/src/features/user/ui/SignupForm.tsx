import {useSignup} from "../api/useSignup.ts";
import {useForm} from "react-hook-form";
import {useSignupSchema, type UserCreateReq} from "@/entities/user/model/types.ts";
import {zodResolver} from "@hookform/resolvers/zod";

export const SignupForm = () => {
  const {
    register,
    handleSubmit,
    formState: {errors, isSubmitting},
  } = useForm<UserCreateReq>({
    resolver: zodResolver(useSignupSchema)
  });

  const {mutate: signup} = useSignup();

  const onSubmit = (data: UserCreateReq) => {
    signup(data);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h1>회원가입</h1>
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
        {isSubmitting ? '가입중..' : '회원가입'}
      </button>
    </form>
  )
}