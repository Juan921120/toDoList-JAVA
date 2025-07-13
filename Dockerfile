# 第一阶段：用 Maven 构建
FROM maven:3.8.8-openjdk-17-slim AS builder
WORKDIR /build
# 只复制 pom.xml、mvn 脚本和源码
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw package -DskipTests

# 第二阶段：运行时镜像
FROM openjdk:17-jdk-slim
WORKDIR /app
# 从构建阶段拷贝 jar
COPY --from=builder /build/target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
