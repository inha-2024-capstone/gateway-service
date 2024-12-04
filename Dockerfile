FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# 소스 코드와 Maven Wrapper 복사
COPY . .

# 빌드된 JAR 파일을 복사
RUN cp /app/build/libs/*SNAPSHOT.jar app.jar

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]