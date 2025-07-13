FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . /app

# 添加执行权限
RUN chmod +x ./gradlew

# 构建应用
RUN ./gradlew build

# 设置容器启动命令
CMD ["java", "-jar", "build/libs/your-app.jar"]
