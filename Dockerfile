FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

# ʹ�� Gradle ��ֱ���������
RUN ./gradlew build

CMD ["java", "-jar", "build/libs/your-app.jar"]
