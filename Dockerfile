# Giai đoạn build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Sao chép pom.xml trước để cache phụ thuộc
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Sao chép mã nguồn
COPY src ./src
RUN mvn clean package -DskipTests

# Giai đoạn chạy ứng dụng
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]