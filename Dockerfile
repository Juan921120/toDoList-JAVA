# ��һ�׶Σ��� Maven ����
FROM maven:3.8.8-openjdk-17-slim AS builder
WORKDIR /build
# ֻ���� pom.xml��mvn �ű���Դ��
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw package -DskipTests

# �ڶ��׶Σ�����ʱ����
FROM openjdk:17-jdk-slim
WORKDIR /app
# �ӹ����׶ο��� jar
COPY --from=builder /build/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
