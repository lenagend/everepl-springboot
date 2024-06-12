# 1. 베이스 이미지로 OpenJDK 21 사용
FROM openjdk:21-jdk-slim AS build

# 2. 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 3. Maven 빌드를 위한 Maven 이미지 사용 (멀티스테이지 빌드)
FROM maven:3.9.5-openjdk-21 AS build
WORKDIR /app

# 4. Maven의 의존성을 설치하기 위해 프로젝트의 POM 파일을 복사
COPY pom.xml .

# 5. Maven 의존성만 먼저 다운로드 (캐시 활용을 위해)
RUN mvn dependency:go-offline -B

# 6. 전체 소스 코드를 복사
COPY src ./src

# 7. Maven을 사용하여 프로젝트를 빌드 (Java 21로 컴파일)
RUN mvn clean package -DskipTests

# 8. 빌드 결과물의 JAR 파일을 가져옴
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 9. 환경 변수를 통해 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 10. 애플리케이션의 포트를 노출
EXPOSE 8080
