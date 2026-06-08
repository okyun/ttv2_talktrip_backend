FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Gradle wrapper + build scripts 먼저 복사 (의존성 캐시 효율)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 소스 복사
COPY src src

# 테스트는 컨테이너 빌드 속도를 위해 기본 스킵 (필요 시 제거)
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Seoul
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]