import {useForm} from "react-hook-form";
import {useUpdatechema, type UserUpdateReq} from "@/entities/user/model/types.ts";
import {zodResolver} from "@hookform/resolvers/zod";
import {useUpdate} from "../api/useUpdate.ts";

export const UpdateForm = () => {
  const {
    register,
    handleSubmit,
    formState: {errors, isSubmitting},
  } = useForm<UserUpdateReq>({
    resolver: zodResolver(useUpdatechema)
  });

  const {mutate: update} = useUpdate();

  const onSubmit = (data: UserUpdateReq) => {
    update(data);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h1>사용자 수정</h1>
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
        {isSubmitting ? '업데이트중..' : '업데이트'}
      </button>
    </form>
  )
}