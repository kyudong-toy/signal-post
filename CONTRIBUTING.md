# Signal Post - Git 가이드

이 문서는 Signal Post 프로젝트의 Git 작업 흐름과 컨벤션을 정의합니다.

## 1. 브랜치 전략 (Branching Strategy)

본 프로젝트는 Feature Branch Workflow를 기반으로 합니다.  
항상 main 브랜치에서 새로운 feature 브랜치를 생성합니다.

- 브랜치 생성 예시: git checkout -b feat/user-login-api

### 1.1. 작업 흐름

기능 개발을 완료한 후, 자신의 feat 브랜치에 코드를 푸시(Push)합니다.  
GitHub에서 main 브랜치로 Pull Request (PR)를 생성합니다.  
코드 리뷰 후 PR이 병합(Merge)되면, 작업했던 feat 브랜치는 삭제합니다.  

## 2. 커밋 메시지 컨벤션 (Commit Message Convention)

모든 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org/ko/v1.0.0/) 명세를 따릅니다. 

```text
type(scope): subject

(optional body)

(optional footer(s))
```

### 2.1. type (필수) : 커밋의 종류
| 타입       | 설명                                 |
|:---------|:-----------------------------------|
| feat     | 새로운 기능(Feature) 추가                 |
| fix      | 버그(Bug) 수정                         |
| docs     | 문서(Documentation) 수정 (README.md 등) |
| style    | 코드 포맷팅, 세미콜론 누락 등 (코드 로직 변경 없음)    |
| refactor | 코드 리팩토링 (기능 변경 없이 내부 구조 개선)        |
| test     | 테스트 코드 추가 또는 수정                    |
| chore    | 소스 코드나 테스트 코드를 수정하지 않는 기타 변경 사항    |

### 2.2. scope (필수): 커밋이 영향을 미치는 범위를 나타냅니다.
해당 프로젝트에서는 도메인(예: user, post)을 적도록 합니다.

### 2.3. subject (필수): 커밋에 대한 50자 이내의 간결한 요약입니다.
명령문으로, 현재 시제로 작성하며 문장 끝에 마침표(.)를 찍지 않습니다.  

```text
feat(user): 사용자 회원가입 API 기능 구현
fix(post): 게시글 조회 시 N+1 문제 해결
docs: README.md에 API 컨벤션 추가
```

### 2.4. body (선택): 커밋에 대한 50자 이내의 간결한 요약입니다.
제목(subject)이 "무엇을" 했는지를 요약한다면, 본문(body)은 "왜" 그리고 "어떻게"했는지를 설명한다.  
선택사항으로 제목에 다 담지 못하는 기술적인 결정이나 복잡한 구현 방식에 대한 설명을 적습니다.

### 2.5. footer (선택): 이슈 ID를 참조합니다.
현재 작업 중인 커밋이 어떤 이슈와 관련이 있다면 작성하도록 합니다.  
이슈 ID는 Pull Request를 참고해주세요.

### BREAKING CHANGE: 중요한 변경 시 !를 붙여 사용합니다.
2가지 선택사항이 있습니다.
1. `scope` 뒤에 붙이기   
   예) refactor(user)!: 사용자 API 엔드포인트 경로를 /api/v1/user으로 변경
2. `footer` 입력전 사용  
   예) BREAKING CHANGE: 사용자 API 엔드포인트가 기존 /api/user에서 /api/v1/user로 변경되었습니다.  
   기존에 요청 방식은 더 이상 지원되지 않습니다.

### 2.6. 커밋 메시지 작성 예시

```text
fix(user): 비밀번호 불일치 시 401 에러 반환

기존에는 비밀번호가 틀렸을 때 400 Bad Request를 반환했으나,
이는 '잘못된 요청'이 아닌 '인증 실패'에 해당하므로
의미에 더 적합한 401 Unauthorized를 반환하도록 수정합니다.

이 변경으로 인해 GlobalExceptionHandler에 InvalidPasswordException을
처리하는 핸들러를 추가했습니다.

BREAKING CHANGE: 로그인 실패 시 반환되는 HTTP 상태 코드가
400에서 401로 변경되었습니다. 클라이언트는 이제 401 코드를
인증 실패로 처리해야 합니다.

Resolves: #42
```

## 3. Pull Request (PR) 컨벤션

Pull Request는 자신의 작업을 `main` 브랜치에 통합하기 위한 **공식 요청**으로 변경 사항을 쉽게 이해할 수 있도록 명확하게 작성해주세요.

### 3.1. PR 제목

PR에 여러 커밋이 있더라도 **하나의 커밋처럼**, 전체 변경 사항을 대표하는 하나의 제목을 작성합니다.
- **좋은 예:** `feat(user): 사용자 회원가입 API 기능 구현`
- **나쁜 예:** `user 기능 추가 및 버그 수정`

### 3.2. PR 본문
- PR 본문은 제공되는 템플릿을 기반으로, **"무엇을, 왜, 어떻게"** 변경했는지 상세하게 작성합니다.

```markdown
### 📝 작업 내용

- 사용자 회원가입 API의 기본 골격을 구현했습니다.
- `UserService`에 비밀번호 암호화 로직을 추가했습니다.

### 💬 리뷰 요구사항

- `PasswordEncoder`를 사용하는 방식이 적절한지 확인해주세요.
- DTO 네이밍 컨벤션을 잘 따랐는지 리뷰 부탁드립니다.
```

### 3.3. 이슈 연결
PR이 해결하는 이슈가 있다면, 본문 마지막에 `Resolves` 키워드와 함께 이슈 번호를 작성합니다.   
이렇게 하면 PR이 병합될 때, 연결된 이슈가 자동으로 닫힙니다.
- **예시:** `Resolves: #42`