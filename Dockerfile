# ʹ�ùٷ� OpenJDK ����
FROM openjdk:17-jdk-slim

# ���ù���Ŀ¼
WORKDIR /app

# ���ƹ����õ� JAR ����������
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# ���� Spring Boot Ӧ��
ENTRYPOINT ["java", "-jar", "app.jar"]
