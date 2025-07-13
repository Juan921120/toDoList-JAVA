# ��һ�׶Σ��� Maven ����
FROM maven:3.8-jdk-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw package -DskipTests

# �ڶ��׶Σ�����ʱ����
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
