# --- 1단계: Gradle을 사용하여 소스 코드 빌드 ---
FROM openjdk:17-jdk-slim as builder
WORKDIR /workspace/app

# 그래들 래퍼와 빌드 파일을 먼저 복사하여 의존성 레이어를 캐싱
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

COPY src ./src
RUN gradle bootJar --no-daemon

# --- 2단계: 실제 실행 환경 구성 ---
FROM openjdk:17-jre-slim
WORKDIR /app

# 생성된 JAR 파일만 복사한다.
COPY --from=builder /workspace/app/build/libs/*.jar back.jar

# 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "back.jar"]