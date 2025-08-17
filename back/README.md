# SignalPost 백엔드 개발 가이드

## 코딩 컨벤션
프로젝트에서 다음과 같은 규칙을 지켜주길 바랍니다.
1. 패키지는 모두 소문자로 사용한다.
2. 클래스 및 인터페이스는 `UpperCamelCase`로 통일한다.
3. 메서드는 `lowerCamelCase`로 통일한다.
4. 메서드는 동사로 어떤 동작을 하는지 명확히 표현한다.
5. 변수는 `lowerCamelCase`로 통일한다.
6. 상수는 `SCREAMING_SNAKE_CASE`로 통일한다.
7. Boolean 변수/메서드는 `is`또는 `has`로 시작한다. 
8. 숫자 리터럴은 상수로 정의하여 매직넘버를 금지한다.  
9. 중괄호는 K&R 스타일(여는 중괄호는 줄 끝에, 닫는 중괄호는 새 줄에)을 따른다.
    ```
    if (condition) {
        // do something
    }
    ```
10. 예외 처리 클래스는 [리소스|상황|Exception]으로 명명한다.
    - UserAlreadyExistsException: 사용자가 이미 존재할 때
    - PostNotFoundException: 게시글을 찾을 수 없을 때
    - CommentNotFoundException: 댓글을 찾을 수 없을 때

## API 컨벤션
1. 주소(URL)는 명사, 행위는 동사로 표현한다.
2. 특정 리소스를 조회, 수정시 URL에 가리켜 사용한다.
   GET /api/users/{userId}
   PATCH /api/posts/{postId}
3. 요청 본문은 생성, 수정시에 사용한다.  
   사용 경우:
    - 전달할 데이터가 복잡한 구조(JSON)를 가질 때 사용합니다.
    - 비밀번호 등 URL에 노출되어서는 안 되는 민감한 데이터를 전달할 때 사용합니다.
    - 주로 POST, PUT, PATCH 메소드와 함께 사용됩니다.
    ###### 예시: POST /api/users 요청 시 {"userName": "kyudong", "password": "..."}
4. DTO의 요청과 응답은 분리한다.  
   요청은 `*ReqDto`  
   응답은 `*ResDto`
5. DTO 클래스명은 도메인 + 행동(동사)로 명명한다.  
   사용자(User) 생성(Create) : UserCreateReqDto

## 테스트 코드 컨벤션
1. 테스트 클래스명은 `*Tests`로 통일한다 
2. 모든 단위 테스트 코드는 Given-When-Then 패턴에 따라 작성하여 테스트의 목적과 과정을 명확하게 드러낸다.  
    Given (준비): 테스트를 실행하기 위해 필요한 전제 조건들을 설정하는 단계. 테스트용 객체를 생성하거나 Mock 객체의 행동을 미리 정의한다.  
    When (실행): 실제로 테스트할 대상 메소드를 실행하는 단계. 이 부분은 보통 한 줄의 코드로 이루어진다.  
    Then (검증): 실행 결과가 예상과 일치하는지, 그리고 의도한 대로 객체 간의 상호작용이 발생했는지 검증하는 단계. Assertions와 Mockito.verify 등을 사용한다.

## API 문서 (Swagger)
본 프로젝트의 API 명세서는 SpringDoc(Swagger)을 통해 자동으로 생성 및 관리됩니다.

### 접속 방법
애플리케이션을 실행한 후, 아래 URL로 접속하여 API를 테스트할 수 있습니다.
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

### 작성 규칙
- API 명세 관련 애너테이션(`@Tag`, `@Operation` 등)은 컨트롤러의 가독성을 위해 **`...Api` 인터페이스에 정의하는 것을 원칙**으로 합니다.

### 테스트 전략
본 프로젝트는 테스트 피라미드 원칙에 입각한 다계층 테스트 전략을 따릅니다.
#### 1. 단위 테스트 (Unit Test) - Mockito 사용

목적: 개별 메소드나 클래스의 비즈니스 로직을 검증합니다.  
대상: Service, Domain(Entity) 계층.  
규칙:
- 의존성은 @Mock으로 가짜 객체를 만들어 완벽하게 격리합니다. 
- 모든 성공, 실패, 엣지 케이스를 꼼꼼하게 테스트합니다. 
- 로직의 정확성은 이 단계에서 100% 보장되어야 합니다.

#### 2. 컨트롤러 테스트 (Controller Test) - @WebMvcTest 사용

목적: 웹 계층의 **'연결성'**과 **'API 계약'**을 검증합니다.  
대상: Controller 계층.  
규칙:  
- @MockitoBean을 사용하여 서비스 계층을 Mock으로 대체합니다.
- 비즈니스 로직을 다시 테스트하지 않습니다.
- 대표적인 성공 케이스 1개 (Happy Path): 요청/응답이 정상적으로 처리되는지 확인합니다.
- 대표적인 실패 케이스 1개 (Unhappy Path): 예외 발생 시 의도한 HTTP 에러를 반환하는지 확인합니다.

#### 3. 통합 테스트 (Integration Test) - @SpringBootTest 사용

목적: 컨트롤러부터 데이터베이스까지, 모든 계층을 관통하는 전체 흐름을 검증합니다.  
대상: 애플리케이션 전체.  
규칙:
- Mock을 최소화하고 실제 빈(Bean)과 실제 DB를 사용합니다.
- 가장 중요하고 핵심적인 사용자 시나리오에 대해서만 작성합니다. (예: 회원가입 후 로그인)
- 각 테스트는 @Transactional을 사용하여 독립성을 보장합니다.
