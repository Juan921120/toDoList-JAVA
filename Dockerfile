FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

# 使用 Gradle 或直接用命令构建
RUN ./gradlew build

CMD ["java", "-jar", "build/libs/your-app.jar"]
