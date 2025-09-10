# Signal-post

해당 문서는 Signal Post 프로젝트의 전체 구조에 대해 설명합니다.

## 프로젝트 세팅
1. HTTPS 키 세팅
   해당 가이드는 mkcert를 기준으로 작성되었습니다.  
   [mkcert](https://github.com/FiloSottile/mkcert)가 설치되어 있어야 합니다.  
   1. 사전준비   
      macOS: `brew install mkcert`  
      Windows: `choco install mkcert`  
      ```text
      # choco가 없다면 아래의 커맨드를 powershell 관리자로 접속 후 설치하면 됩니다.
      Set-ExecutionPolicy Bypass -Scope Process -Force; `
      [System.Net.ServicePointManager]::SecurityProtocol = `
      [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; `
      iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
      ```
   2. 로컬 인증 기관(CA) 설치  
      mkcert -install
   3. 인증서 생성
      편한 세팅을 위해서 프로젝트 루트에 infra/nginx/certs 폴더에 설치하면 됩니다.  
      mkcert localhost 127.0.0.1 ::1  
      파일의 이름은 local.crt, local.key로 지정시 빠른 환경 구성이 가능합니다.
      > 해당 개인용 인증서 파일은 git에 포함되지 않습니다.

2. PostgreSQL
   프로젝트에서 메인 데이터베이스는 PostgreSQL로 자세한 정보는 docker-compose-dev를 확인해주세요.  
3. Redis
   Redis로 캐싱 및 데이터를 보조로 다루고 있습니다 redis/redis-dev를 참고해주세요.  

## API 명세서
본 프로젝트의 API 명세서는 Swagger UI를 통해 확인할 수 있습니다.
- [로컬 Swagger UI 바로가기](http://localhost:8080/swagger-ui.html) (로컬 서버 실행 필요)