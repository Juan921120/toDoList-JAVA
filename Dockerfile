FROM openjdk:17-jdk-slim

# ���ù���Ŀ¼
WORKDIR /app

# ������Ŀ�ļ�
COPY . /app

# ���ִ��Ȩ��
RUN chmod +x ./gradlew

# ����Ӧ��
RUN ./gradlew build

# ����������������
CMD ["java", "-jar", "build/libs/your-app.jar"]
