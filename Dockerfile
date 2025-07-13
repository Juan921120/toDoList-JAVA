# ---- �����׶� ----
FROM maven:3.9.0-openjdk-17-slim AS builder
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B -ntp dependency:go-offline
COPY src src
RUN mvn -B -ntp package -DskipTests

# ---- ���н׶� ----
FROM openjdk:17-jdk-slim
WORKDIR /app
# ע������Ҫ������ mvn ����������ļ�һ��
COPY --from=builder /workspace/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
