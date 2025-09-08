FROM openjdk:17-jdk-slim as stage1

WORKDIR /app

COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .

RUN ./gradlew bootJar

# 2 스테이지 패턴(실제로 서버를 실행시키기 위해서는 jar만 필요하기 때문에 사용하는 방식, 불필요한 파일들을 제외시켜 용량을 줄일 수 있음)
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=stage1 /app/build/libs/*.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]

# 실행시 localhost로는 접속이 안되기 때문에 host.docker.internal로 주입하여 컨테이너 생성
# docker-practice % docker run --name ordersystem-container -d -p 8080:8080 -e SPRING_REDIS_HOST=host.docker.internal -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.dock
# er.internal:3306/ordersystem ordersystem:v1.0 