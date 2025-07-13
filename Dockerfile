# ---- 构建阶段 ----
FROM maven:3.9.0-openjdk-17-slim AS builder
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN mvn -B -ntp dependency:go-offline
COPY src src
RUN mvn -B -ntp package -DskipTests

# ---- 运行阶段 ----
FROM openjdk:17-jdk-slim
WORKDIR /app
# 注意名称要和上面 mvn 打包出来的文件一致
COPY --from=builder /workspace/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
