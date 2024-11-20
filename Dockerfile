# 1. OpenJDK 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 빌드된 JAR 파일을 Docker 이미지에 복사
ARG JAR_FILE=build/libs/newReals_BE.jar
COPY ${JAR_FILE} app.jar

# 3. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
