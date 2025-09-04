import {Link} from "@/shared/ui";

export const Header = () => [
  <header>
    <nav>
      <Link to={'/'}>홈</Link>
      <Link to={'/login'}>로그인</Link>
      <Link to={'/signup'}>회원가입</Link>
      <Link to={'/me'}>자기프로필</Link>
      <Link to={'/write'}>글작성</Link>
      <Link to={'/post/2'}>테스트게시글</Link> {/*todo : 하드코딩 수정필요*/}
      <Link to={'/chat'}>테스트채팅방생성</Link> {/*todo : 하드코딩 수정필요*/}
      <Link to={'/chatrooms'}>테스트채팅방목록</Link> {/*todo : 하드코딩 수정필요*/}
    </nav>
  </header>
]